package com.example.foodfight.comments;

import com.example.foodfight.upload.Upload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long>{
    Page<Comments> findByUpload(Upload upload, Pageable pageable);
    List<Comments> findByUpload(Upload upload);
}