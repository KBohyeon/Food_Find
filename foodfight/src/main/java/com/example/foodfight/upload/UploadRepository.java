package com.example.foodfight.upload;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.foodfight.comments.Comments;


public interface UploadRepository extends JpaRepository<Upload, Long>{
//    List<Upload> findByUpload(Upload upload);
    List<Upload> findByCategoryOrderByRatingDesc(String category); //카테고리별 평점 높은순 정리
    
    @Query("SELECT u FROM Upload u WHERE " +
            "LOWER(u.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.menu1) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.menu2) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.tag1) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.tag2) LIKE LOWER(CONCAT('%', :keyword, '%'))")
     List<Upload> searchByKeyword(@Param("keyword") String keyword);
    
}
