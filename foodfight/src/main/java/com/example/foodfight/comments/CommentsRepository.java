package com.example.foodfight.comments;

import com.example.foodfight.upload.Upload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long>{
	//페이징 처리
    Page<Comments> findByUpload(Upload upload, Pageable pageable);
    //기본 조회
    List<Comments> findByUpload(Upload upload);
    
    
    
    // 날짜 기준 내림차순 정렬 (최신순)
    List<Comments> findByUploadOrderByCreateDateDesc(Upload upload);
    
    // 평점 기준 내림차순 정렬 (평점 높은순)
    List<Comments> findByUploadOrderByRatingDesc(Upload upload);
    
    // 평점 기준 오름차순 정렬 (평점 낮은순)
    List<Comments> findByUploadOrderByRatingAsc(Upload upload);
}