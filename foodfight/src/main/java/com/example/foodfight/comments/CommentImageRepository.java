package com.example.foodfight.comments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentImageRepository extends JpaRepository<CommentImage, Long> {
	//이미지
	List<CommentImage> findByComment(Comments comment);
    List<CommentImage> findByCommentId(Long commentId);
}