package com.example.English.teaching.center.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;


@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file){
        try{
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap("folder", "avatars"));

            return uploadResult.get("url").toString();
        }catch(Exception e){
            throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    public void deleteFile(String publicId){
        try{
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }catch(IOException e){
            throw new RuntimeException("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }

    public String extractPublicId(String url){
        if(url == null || !url.contains("cloudinary")) return null;

        try{
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1];
            String folderPart = parts[parts.length - 2];
            String publicIdWithExtension = folderPart + "/" + lastPart;
            return publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf("."));
        } catch (Exception e) {
            return null;
        }
    }
}