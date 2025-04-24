package com.example.foodfight.comments;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentImageService {

    private final CommentImageRepository commentImageRepository;
    
    @Transactional(readOnly = true)
    public List<CommentImage> getImagesByCommentId(Long commentId) {
        return commentImageRepository.findByCommentId(commentId);
    }
    
    @Transactional
    public void deleteImage(Long imageId) {
        commentImageRepository.deleteById(imageId);
    }
}