package com.studyroom.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload.image.path}")
    private String imageUploadPath;

    @Value("${file.upload.image.url}")
    private String imageUploadUrl;

    /**
     * 上传图片
     * @param file 图片文件
     * @return 图片访问路径
     * @throws IOException 上传失败时抛出异常
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 创建上传目录（如果不存在）
        File uploadDir = new File(imageUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String fileName = UUID.randomUUID().toString() + suffix;

        // 按日期创建子目录
        String datePath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateDir = new File(uploadDir, datePath);
        if (!dateDir.exists()) {
            dateDir.mkdirs();
        }

        // 保存文件
        File destFile = new File(dateDir, fileName);
        file.transferTo(destFile);

        // 返回文件访问路径
        return imageUploadUrl + datePath + "/" + fileName;
    }

    /**
     * 删除图片
     * @param imagePath 图片访问路径
     * @return 是否删除成功
     */
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        // 从访问路径中提取文件路径
        String relativePath = imagePath.replace(imageUploadUrl, "");
        File file = new File(imageUploadPath + relativePath);

        if (file.exists()) {
            return file.delete();
        }

        return false;
    }
}
