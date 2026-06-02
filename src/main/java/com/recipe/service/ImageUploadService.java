package com.recipe.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Log4j2
public class ImageUploadService {

    @Value("${image.upload-dir:/app/uploads/recipes}")
    private String uploadDir;

    @Value("${image.base-url:http://localhost:8080/studio-recipe}")
    private String baseUrl;

    public String upload(MultipartFile file) {
        // 파일 검증
        validateFile(file);

        // 저장 경로 생성
        Path uploadPath = Paths.get(uploadDir);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // UUID로 고유 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFilename = UUID.randomUUID() + extension;

            // 파일 저장
            Path filePath = uploadPath.resolve(savedFilename);
            Files.write(filePath, file.getBytes());

            log.info("이미지 저장 완료: {}", filePath);

            // 접근 URL 반환
            return baseUrl + "/images/" + savedFilename;

        } catch (IOException e) {
            log.error("이미지 저장 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 저장에 실패했습니다.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 크기 제한 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다.");
        }

        // 이미지 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }
}
