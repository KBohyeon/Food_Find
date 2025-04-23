package com.example.foodfight.comments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload.directory:uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String subDir) {
        try {
            // 업로드 디렉토리 생성
            String directory = uploadDir + File.separator + subDir;
            Path directoryPath = Paths.get(directory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            
            // 파일명 생성 (UUID 사용)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 파일 저장
            Path filePath = Paths.get(directory + File.separator + newFilename);
            Files.write(filePath, file.getBytes());
            
            // 파일 URL 반환 (상대 경로)
            System.out.println("파일이 저장될 경로: " + directory);
            System.out.println("저장할 파일 경로: " + filePath.toString());
            return "/" + subDir + "/" + newFilename;
            
        } catch (IOException e) {
        	
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}