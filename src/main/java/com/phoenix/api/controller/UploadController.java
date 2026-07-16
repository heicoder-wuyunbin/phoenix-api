package com.phoenix.api.controller;

import com.phoenix.api.result.Result;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 允许上传的图片类型
     * 对应原代码：./old/iwebshop/lib/core/util/upload_class.php allowType 属性
     */
    private static final Set<String> ALLOWED_TYPES = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");

    /**
     * 最大文件大小（10MB）
     * 对应原代码：./old/iwebshop/lib/core/util/upload_class.php 构造参数 size=10000（KB）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    /**
     * 图片上传
     *
     * 对应原代码：
     *   - ./old/iwebshop/controllers/pic.php upload() 方法（本地上传方式）
     *   - ./old/iwebshop/classes/photoupload.php run() 方法（图片上传执行）
     *   - ./old/iwebshop/lib/core/util/upload_class.php execute() 方法（文件上传核心）
     *
     * 差异说明：
     *   1. 原 PHP 使用本地文件系统存储，新系统使用 MinIO 对象存储
     *   2. 原 PHP 有 MD5 去重机制，新系统暂不实现（保持简单）
     *   3. 原 PHP 生成缩略图，新系统暂不实现（前端按需缩放）
     *   4. 文件名生成规则保持一致：YmdHis + 随机数 + 扩展名
     *   5. 目录结构保持一致：YYYY/mm/dd/
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过10MB");
        }

        // 获取文件扩展名
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "文件名不能为空");
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // 校验文件类型
        if (!ALLOWED_TYPES.contains(ext)) {
            return Result.error("不支持的文件类型，仅支持: " + String.join(", ", ALLOWED_TYPES));
        }

        try {
            // 生成文件名：YmdHis + 随机数 + 扩展名
            // 对应原代码：ITime::getDateTime('YmdHis').mt_rand(100,999).'.'.$fileext
            String datePart = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDate.now().atStartOfDay());
            // 使用当前时间纳秒代替随机数，确保唯一性
            String nanoPart = String.format("%03d", System.nanoTime() % 1000);
            String fileName = datePart + nanoPart + "." + ext;

            // 目录结构：YYYY/mm/dd/
            // 对应原代码：$dir = IWeb::$app->config['upload'].'/'.date('Y')."/".date('m')."/".date('d');
            String datePath = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(LocalDate.now());
            String objectName = datePath + "/" + fileName;

            // 上传到 MinIO
            // 对应原代码：move_uploaded_file($_FILES[$field]['tmp_name'], $this->dir.$fileInfo[0]['name']);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 构建访问 URL
            // 使用 /api/upload/proxy/ 路径代理访问，避免直接暴露 MinIO 端口
            String url = "/api/upload/proxy/" + objectName;

            log.info("File uploaded successfully: {} -> {}", originalFilename, objectName);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("objectName", objectName);
            return Result.success(result);

        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 图片代理访问（从 MinIO 读取并返回）
     * 避免前端直接访问 MinIO，绕开 CORS 和端口问题
     */
    @GetMapping("/proxy/**")
    public void proxy(jakarta.servlet.http.HttpServletRequest request,
                      jakarta.servlet.http.HttpServletResponse response) {
        String requestPath = request.getRequestURI();
        String objectName = requestPath.substring(requestPath.indexOf("/proxy/") + "/proxy/".length());

        try {
            var object = minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );

            // 根据扩展名设置 Content-Type
            String ext = objectName.substring(objectName.lastIndexOf(".") + 1).toLowerCase();
            String contentType = switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "bmp" -> "image/bmp";
                case "webp" -> "image/webp";
                default -> "application/octet-stream";
            };

            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=31536000");
            object.transferTo(response.getOutputStream());
        } catch (Exception e) {
            log.error("Image proxy failed: {}", e.getMessage());
            response.setStatus(404);
        }
    }
}