package com.example.foodfight.upload;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadImageRepository extends JpaRepository<UploadImage, Long> {
    List<UploadImage> findByUpload(Upload upload);
    List<UploadImage> findByUploadId(Long uploadId);
}
