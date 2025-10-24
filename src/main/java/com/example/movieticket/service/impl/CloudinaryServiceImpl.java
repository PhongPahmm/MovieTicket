package com.example.movieticket.service.impl;

import com.cloudinary.Cloudinary;
import com.example.movieticket.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    public String upload(MultipartFile file) {
        try {
            String contentType = file.getContentType();

            if (contentType == null) {
                throw new RuntimeException("File has no content type");
            }
            Map<String, Object> uploadOptions;

            if (contentType.startsWith("video/")) {
                uploadOptions = Map.of(
                        "resource_type", "video",
                        "folder", "videos"
                );
                var data = cloudinary.uploader().uploadLarge(file.getBytes(), uploadOptions);
                return data.get("secure_url").toString();
            } else if (contentType.startsWith("image/")) {
                uploadOptions = Map.of(
                        "resource_type", "image",
                        "folder", "images"
                );
                var data = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
                return data.get("secure_url").toString();

            } else {
                throw new RuntimeException("Invalid image or video file type");
            }

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

}