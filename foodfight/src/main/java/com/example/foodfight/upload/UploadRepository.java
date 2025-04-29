package com.example.foodfight.upload;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UploadRepository extends JpaRepository<Upload, Long>{
//    List<Upload> findByUpload(Upload upload);
    List<Upload> findByCategory(String category);
}
