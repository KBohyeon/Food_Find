package com.example.foodfight.comments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.foodfight.upload.Upload;
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
	private final UploadService uploadService; // UploadService 의존성 추가
    
	public void create(Upload upload, String content, SiteUser author) {
		Comments comments = new Comments();
		comments.setContent(content);
		comments.setCreateDate(LocalDateTime.now());
		comments.setUpload(upload);
        comments.setAuthor(author);
		this.commentsRepository.save(comments);
	}
	
	// 평점과 작성자 정보를 포함한 리뷰 생성 메서드
	@Transactional
	public Comments create(Upload upload, String content, SiteUser author, Double rating) {
		Comments comments = new Comments();
		comments.setContent(content);
		comments.setCreateDate(LocalDateTime.now());
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
	        comments.setCreateDate(LocalDateTime.now());
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
	
	// 리뷰 목록 조회 (페이징 및 정렬 기능 포함)
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
	
	// 업로드에 대한 모든 리뷰 조회
	public List<Comments> getCommentsByUpload(Upload upload) {
		return commentsRepository.findByUpload(upload);
	}
	
	// 업로드 평점 업데이트
	@Transactional
	private void updateUploadRating(Upload upload) {
		List<Comments> allComments = commentsRepository.findByUpload(upload);
		
		if (allComments.isEmpty()) {
			upload.setRating(0.0);
		} else {
			double sum = 0;
			int count = 0;
			
			for (Comments comment : allComments) {
				if (comment.getRating() != null) {
					sum += comment.getRating();
					count++;
				}
			}
			
			if (count > 0) {
				double avgRating = sum / count;
				upload.setRating(Math.round(avgRating * 10) / 10.0); // 소수점 첫째 자리까지 반올림
			} else {
				upload.setRating(0.0);
			}
		}
		
		// 리뷰 수 업데이트
		upload.setReviewCount(allComments.size());
		
		// 업로드 저장
		uploadService.save(upload);
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
}
