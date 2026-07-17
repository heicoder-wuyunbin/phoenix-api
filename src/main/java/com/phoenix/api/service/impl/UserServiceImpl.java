package com.phoenix.api.service.impl;

import com.phoenix.api.config.SiteConfig;
import com.phoenix.api.dto.request.LoginDTO;
import com.phoenix.api.dto.request.RegisterDTO;
import com.phoenix.api.dto.response.LoginResponseDTO;
import com.phoenix.api.entity.MemberEntity;
import com.phoenix.api.entity.UserEntity;
import com.phoenix.api.exception.BusinessException;
import com.phoenix.api.mapper.MemberMapper;
import com.phoenix.api.mapper.UserGroupMapper;
import com.phoenix.api.mapper.UserMapper;
import com.phoenix.api.service.UserService;
import com.phoenix.api.util.CaptchaUtil;
import com.phoenix.api.util.JwtUtil;
import com.phoenix.api.util.MD5Util;
import com.phoenix.api.util.SmsUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final MemberMapper memberMapper;
    private final UserGroupMapper userGroupMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final SiteConfig siteConfig;
    private final CaptchaUtil captchaUtil;
    private final SmsUtil smsUtil;

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        UserEntity user = userMapper.findByLoginInfo(loginDTO.getLoginInfo());
        if (user == null) {
            throw new BusinessException("未找到用户信息");
        }

        String encryptedPassword = MD5Util.encode(loginDTO.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("未找到用户信息");
        }

        // 检查会员状态
        MemberEntity member = memberMapper.selectOne(Wrappers.lambdaQuery(MemberEntity.class)
                .eq(MemberEntity::getUserId, user.getId()));
        if (member != null) {
            if (member.getStatus() == 3) {
                throw new BusinessException("账号未激活，请先验证邮箱");
            }
            if (member.getStatus() != 1) {
                throw new BusinessException("账号已被禁用");
            }
        }

        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        Integer regOption = siteConfig.getRegOption();

        if (regOption == 2) {
            throw new BusinessException("当前网站禁止新用户注册");
        }

        if (!registerDTO.getPassword().equals(registerDTO.getRepassword())) {
            throw new BusinessException("2次密码输入不一致");
        }

        if (!captchaUtil.verify(registerDTO.getCaptchaKey(), registerDTO.getCaptcha())) {
            throw new BusinessException("图形验证码输入不正确");
        }

        String regType = registerDTO.getRegType();
        if (regType == null || regType.isEmpty()) {
            throw new BusinessException("请选择注册方式");
        }

        if ("email".equals(regType)) {
            if (!CaptchaUtil.isEmail(registerDTO.getEmail())) {
                throw new BusinessException("邮箱格式不正确");
            }
            MemberEntity existingMember = memberMapper.findByEmail(registerDTO.getEmail());
            if (existingMember != null) {
                if (existingMember.getStatus() == 3) {
                    sendCheckMail(registerDTO.getEmail());
                    return;
                }
                throw new BusinessException("邮箱已经被注册");
            }
        } else if ("mobile".equals(regType)) {
            if (!CaptchaUtil.isMobile(registerDTO.getMobile())) {
                throw new BusinessException("手机号格式不正确");
            }
            String storedCode = redisTemplate.opsForValue().get("code:" + registerDTO.getMobile());
            if (!registerDTO.getMobileCode().equals(storedCode)) {
                throw new BusinessException("手机号验证码不正确");
            }
            MemberEntity existingMember = memberMapper.findByMobile(registerDTO.getMobile());
            if (existingMember != null) {
                throw new BusinessException("手机号已经被注册");
            }
        } else {
            throw new BusinessException("不支持的注册方式");
        }

        UserEntity existingUser = userMapper.selectOne(Wrappers.lambdaQuery(UserEntity.class)
                .eq(UserEntity::getUsername, registerDTO.getUsername()));
        if (existingUser != null) {
            throw new BusinessException("用户名已经被注册");
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(MD5Util.encode(registerDTO.getPassword()));
        userMapper.insert(user);

        MemberEntity member = new MemberEntity();
        member.setUserId(user.getId());
        member.setTime(LocalDateTime.now());
        member.setStatus("email".equals(regType) ? 3 : 1);
        member.setMobile(registerDTO.getMobile());
        member.setEmail(registerDTO.getEmail());
        memberMapper.insert(member);

        if ("email".equals(regType)) {
            sendCheckMail(registerDTO.getEmail());
        }
    }

    @Override
    public void sendMobileCode(String mobile, String captchaKey, String captcha) {
        if (!CaptchaUtil.isMobile(mobile)) {
            throw new BusinessException("请填写正确的手机号码");
        }
        if (!captchaUtil.verify(captchaKey, captcha)) {
            throw new BusinessException("请填写正确的图形验证码");
        }

        MemberEntity existingMember = memberMapper.findByMobile(mobile);
        if (existingMember != null) {
            throw new BusinessException("手机号已经被注册");
        }

        String mobileCode = String.valueOf(new Random().nextInt(9000) + 1000);
        SmsUtil.send(mobile, mobileCode);
        redisTemplate.opsForValue().set("code:" + mobile, mobileCode != null ? mobileCode : "", 5, TimeUnit.MINUTES);
    }

    @Override
    public void logout() {
    }

    @Override
    @Transactional
    public void checkMail(String code) {
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(code));
            String[] parts = decoded.split("\\|");
            String email = parts[0];
            Long userId = Long.parseLong(parts[1]);

            if (!CaptchaUtil.isEmail(email)) {
                throw new BusinessException("邮箱格式不正确");
            }

            MemberEntity memberRow = memberMapper.selectOne(Wrappers.lambdaQuery(MemberEntity.class)
                    .eq(MemberEntity::getEmail, email)
                    .eq(MemberEntity::getUserId, userId));

            if (memberRow != null) {
                memberRow.setStatus(1);
                memberMapper.updateById(memberRow);
            } else {
                throw new BusinessException("验证信息有误，请核实！");
            }
        } catch (Exception e) {
            throw new BusinessException("验证信息有误，请核实！");
        }
    }

    @Override
    public void sendCheckMail(String email) {
        if (!CaptchaUtil.isEmail(email)) {
            throw new BusinessException("邮件格式错误");
        }

        MemberEntity memberRow = memberMapper.findByEmail(email);
        if (memberRow == null) {
            throw new BusinessException("用户信息不存在");
        }

        String code = java.util.Base64.getEncoder().encodeToString((email + "|" + memberRow.getUserId()).getBytes());
        String url = siteConfig.getBaseUrl() + "/api/user/check-mail-page?code=" + code;
        smsUtil.sendMail(email, url);
    }

    @Override
    public void updatePassword(Long userId, Map<String, Object> params) {
        // TODO: 实现修改密码逻辑
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) {
        // TODO: 实现头像上传逻辑
        return null;
    }

    private LoginResponseDTO buildLoginResponse(UserEntity user) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setHeadIco(user.getHeadIco());
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));

        MemberEntity member = memberMapper.selectOne(Wrappers.lambdaQuery(MemberEntity.class)
                .eq(MemberEntity::getUserId, user.getId()));
        if (member != null) {
            member.setLastLogin(LocalDateTime.now());
            memberMapper.updateById(member);

            Long groupId = userGroupMapper.findGroupIdByExp(member.getExp() != null ? member.getExp() : 0);
            if (groupId != null) {
                member.setGroupId(groupId.intValue());
                memberMapper.updateById(member);
            }
        }

        return response;
    }
}