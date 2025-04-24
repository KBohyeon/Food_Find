package com.example.foodfight.comments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadRepository;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Optional;
import com.example.foodfight.DataNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import com.example.foodfight.user.SiteUser;
import org.springframework.web.multipart.MultipartFile;



import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentsService {
	
	private final CommentsRepository commentsRepository;
	private final CommentImageRepository commentImageRepository;
	private final FileService fileService;
	private final UploadService uploadService; 
	private final UploadRepository uploadRepository;
    
	public void create(Upload upload, String content, SiteUser author) {
		Comments comments = new Comments();
		comments.setContent(content);
		comments.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		comments.setUpload(upload);
        comments.setAuthor(author);
		this.commentsRepository.save(comments);
	}
	
	// 평점과 작성자 정보를 포함한 리뷰 생성 메서드
	@Transactional
	public Comments create(Upload upload, String content, SiteUser author, Double rating) {
		Comments comments = new Comments();
		comments.setContent(content);
		comments.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		comments.setUpload(upload);
		comments.setAuthor(author);
		comments.setRating(rating);
		
		Comments savedComments = this.commentsRepository.save(comments);
		
		// 업로드 평점 업데이트
		updateUploadRating(upload);
		
		return savedComments;
	}
	
	// 이미지 업로드를 포함한 리뷰 생성 메서드
	@Transactional
	public Comments create(Upload upload, String content, SiteUser author, Double rating, List<MultipartFile> images) {
	    try {
	        Comments comments = new Comments();
	        comments.setContent(content);
	        comments.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	        comments.setUpload(upload);
	        comments.setAuthor(author);
	        comments.setRating(rating);
	        
	        // 리뷰 저장
	        Comments savedComments = this.commentsRepository.save(comments);
	        
	        // 이미지 처리
	        if (images != null && !images.isEmpty()) {
	            List<CommentImage> commentImages = new ArrayList<>();
	            
	            for (MultipartFile image : images) {
	                if (!image.isEmpty()) {
	                    // 이미지 파일 저장 및 URL 가져오기
	                    String imageUrl = fileService.saveFile(image, "comments");
	                    
	                    // 이미지 엔티티 생성 및 저장
	                    CommentImage commentImage = new CommentImage();
	                    commentImage.setComment(savedComments);
	                    commentImage.setUrl(imageUrl);
	                    commentImageRepository.save(commentImage);
	                    
	                    commentImages.add(commentImage);
	                }
	            }
	            
	            // 리뷰에 이미지 목록 설정
	            savedComments.setImages(commentImages);
	        }
	        
	        // 업로드 평점 업데이트
	        updateUploadRating(upload);
	        
	        return savedComments;
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException("리뷰 생성 중 오류가 발생했습니다: " + e.getMessage());
	    }
	}
	
	// 리뷰 목록 조회 (페이징 및 정렬 기능 포함) 작동안함 고쳐야함 
	public List<Comments> getCommentsByUpload(Upload upload, int page, int size, String sort) {
		Pageable pageable;
		
		// 정렬 방식에 따른 Pageable 객체 생성
		if ("highest".equals(sort)) {
			pageable = PageRequest.of(page, size, Sort.by("rating").descending());
		} else if ("lowest".equals(sort)) {
			pageable = PageRequest.of(page, size, Sort.by("rating").ascending());
		} else { // "latest"
			pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
		}
		
		Page<Comments> commentsPage = commentsRepository.findByUpload(upload, pageable);
		return commentsPage.getContent();
	}
	
	// 업로드에 대한 모든 리뷰 기본 조회
	public List<Comments> getCommentsByUpload(Upload upload) {
		return commentsRepository.findByUpload(upload);
	}
	
    // 최신순 정렬 메서드
    public List<Comments> getCommentsByUploadLatest(Upload upload) {
        return commentsRepository.findByUploadOrderByCreateDateDesc(upload);
    }
    
    // 평점 높은순 정렬 메서드
    public List<Comments> getCommentsByUploadHighestRating(Upload upload) {
        return commentsRepository.findByUploadOrderByRatingDesc(upload);
    }
    
    // 평점 낮은순 정렬 메서드
    public List<Comments> getCommentsByUploadLowestRating(Upload upload) {
        return commentsRepository.findByUploadOrderByRatingAsc(upload);
    }
    
    public Page<Comments> getCommentsByUploadPaged(Upload upload, Pageable pageable) {
        return commentsRepository.findByUpload(upload, pageable);
    }

 // 업로드 평점 업데이트
    @Transactional
    private void updateUploadRating(Upload upload) {
        List<Comments> allComments = commentsRepository.findByUpload(upload);
        uploadService.updateRating(upload, allComments);
    }
	
	
    public void vote(Comments comments, SiteUser siteUser) {
    	comments.getVoter().add(siteUser);
        this.commentsRepository.save(comments);
    }

	public Comments getComments(Long id) {
        Optional<Comments> comments = this.commentsRepository.findById(id);
        if (comments.isPresent()) {
            return comments.get();
        } else {
            throw new DataNotFoundException("comments not found");
        }
	}
	
	public Comments getComments(Integer id) {
	    return getComments(id.longValue());
	}
	
	//리뷰 수정
    public void modify(Upload upload, String content, SiteUser author, Double rating, List<MultipartFile> images) {
        Comments comments = new Comments();
    	comments.setContent(content);
        comments.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        comments.setUpload(upload);
        comments.setAuthor(author);
        comments.setRating(rating);
        comments.setModifyDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.commentsRepository.save(comments);
    }
	
	//이미지
	public List<CommentImage> getCommentImagesByComment(Comments comment) {
	    return commentImageRepository.findByComment(comment);
	}
	
	//수정
	@Transactional
	public Comments update(Comments comment, String content, Double rating, List<MultipartFile> images, List<Long> deleteImageIds) {
	    // 내용과 평점 업데이트
	    comment.setContent(content);
	    comment.setRating(rating);
	    
	    // 삭제할 이미지 처리
	    if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
	        for (Long imageId : deleteImageIds) {
	            commentImageRepository.deleteById(imageId);
	        }
	    }
	    
	    // 새 이미지 추가
	    if (images != null && !images.isEmpty()) {
	        for (MultipartFile image : images) {
	            if (!image.isEmpty()) {
	                // 이미지 파일 저장 및 URL 가져오기
	                String imageUrl = fileService.saveFile(image, "comments");
	                
	                // 이미지 엔티티 생성 및 저장
	                CommentImage commentImage = new CommentImage();
	                commentImage.setComment(comment);
	                commentImage.setUrl(imageUrl);
	                commentImageRepository.save(commentImage);
	            }
	        }
	    }
	    
	    // 리뷰 저장
	    Comments updatedComment = commentsRepository.save(comment);
	    
	    // 업로드 평점 업데이트
	    updateUploadRating(comment.getUpload());
	    
	    return updatedComment;
	}

	
}
