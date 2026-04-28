package com.example.English.teaching.center.service.infra;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map<String, String> uploadFileSecure(MultipartFile file, String uuidFileName){
        try(InputStream inputStream = file.getInputStream()){ 
            Map uploadResult = cloudinary.uploader().upload(inputStream, 
                ObjectUtils.asMap(
                    "folder", "avatars",
                    "public_id", uuidFileName,
                    "overwrite", true,
                    "invalidate", true,
                    "resource_type", "image",
                    "format", "webp"
                ));

            Map<String, String> result = new HashMap<>();
            result.put("url", uploadResult.get("url").toString());
            result.put("public_id", uploadResult.get("public_id").toString());
            return result;
        }catch(Exception e){
            log.error("Cloudinary upload failed: ", e);
            throw new RuntimeException("Lỗi tải ảnh lên server lưu trữ.");
        }
    }

    public void deleteFile(String publicId){
        if(publicId == null || publicId.isEmpty()) return;
        
        int maxRetries = 3;
        int count = 0;

        while(count < maxRetries){
            try{
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Đã xóa file trên Cloudinary: {}", publicId);
                return;
            }catch(IOException e){
                count++;
                log.warn("Lần thử {} xóa ảnh {} thất bại. Đang đợi thử lại...", count, publicId);
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
            log.error("Thất bại hoàn toàn khi xóa ảnh {} sau 3 lần thử.", publicId);
        }

    }
}