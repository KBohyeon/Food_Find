package com.example.foodfight.upload;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import java.util.Optional;
import com.example.foodfight.DataNotFoundException;
import com.example.foodfight.comments.CommentImage;
import com.example.foodfight.comments.Comments;
import com.example.foodfight.comments.FileService;
import com.example.foodfight.user.SiteUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.transaction.Transactional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
@Service
public class UploadService {

    private final UploadRepository uploadRepository;
    private final UploadImageRepository uploadImageRepository;
    private final FileService fileService;
    
    
    // 기본 식당 등록 메서드
    public void create(String subject, String content, String category) {
        Upload upload = new Upload();
        upload.setSubject(subject);
        upload.setContent(content);
        upload.setCategory(category);
        upload.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.uploadRepository.save(upload);
    }
    
    // 위치 정보와 이미지 경로를 포함한 식당 등록 메서드
    public void create(String subject, String content, String category, String location, String imagePath) {
        Upload upload = new Upload();
        upload.setSubject(subject);
        upload.setContent(content);
        upload.setCategory(category);
        upload.setLocation(location);
        upload.setImagePath(imagePath);
        upload.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.uploadRepository.save(upload);
    }
    
    // 이미지 업로드를 포함한 식당 등록 메서드
    @Transactional
    public Upload create(String subject, String content, String category, String location, String menu1, String menu1Price, 
    					 String menu2, String menu2Price, String menu3, String menu3Price, String openTime, String closeTime,
    					 String dayOff, String phone, String tag1, String tag2,
                        MultipartFile mainImage, List<MultipartFile> additionalImages, SiteUser author) {
        try {
            Upload upload = new Upload();
            upload.setSubject(subject);
            upload.setContent(content);
            upload.setCategory(category);
            upload.setLocation(location);
            upload.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            upload.setAuthor(author);
            upload.setMenu1(menu1);
            upload.setMenu1Price(menu1Price);
            upload.setMenu2(menu2);
            upload.setMenu2Price(menu2Price);
            upload.setMenu3(menu3);
            upload.setMenu3Price(menu3Price);
            upload.setOpenTime(openTime);
            upload.setCloseTime(closeTime);
            upload.setDayOff(dayOff);
            upload.setPhone(phone);
            upload.setTag1(tag1);
            upload.setTag2(tag2);
            
            // 대표 이미지 처리
            if (mainImage != null && !mainImage.isEmpty()) {
                String mainImageUrl = fileService.saveFile(mainImage, "restaurants");
                upload.setImagePath(mainImageUrl);
            }
            
            // 식당 저장
            Upload savedUpload = this.uploadRepository.save(upload);
            
            // 추가 이미지 처리
            if (additionalImages != null && !additionalImages.isEmpty()) {
                List<UploadImage> uploadImages = new ArrayList<>();
                
                for (MultipartFile image : additionalImages) {
                    if (!image.isEmpty()) {
                        // 이미지 파일 저장 및 URL 가져오기
                        String imageUrl = fileService.saveFile(image, "restaurants");
                        
                        // 이미지 엔티티 생성 및 저장
                        UploadImage uploadImage = new UploadImage();
                        uploadImage.setUpload(savedUpload);
                        uploadImage.setUrl(imageUrl);
                        uploadImageRepository.save(uploadImage);
                        
                        uploadImages.add(uploadImage);
                    }
                }
                
                // 식당에 이미지 목록 설정
                savedUpload.setImages(uploadImages);
            }
            
            return savedUpload;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("식당 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 식당 정보 수정 메서드
    @Transactional
    public Upload update(Upload upload, String subject, String content, String category, String location,
                        MultipartFile mainImage, List<MultipartFile> additionalImages, List<Long> deleteImageIds) {
        // 기본 정보 업데이트
        upload.setSubject(subject);
        upload.setContent(content);
        upload.setCategory(category);
        upload.setLocation(location);
        upload.setModifyDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 대표 이미지 업데이트
        if (mainImage != null && !mainImage.isEmpty()) {
            String mainImageUrl = fileService.saveFile(mainImage, "restaurants");
            upload.setImagePath(mainImageUrl);
        }
        
        // 삭제할 이미지 처리
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imageId : deleteImageIds) {
                uploadImageRepository.deleteById(imageId);
            }
        }
        
        // 추가 이미지 처리
        if (additionalImages != null && !additionalImages.isEmpty()) {
            for (MultipartFile image : additionalImages) {
                if (!image.isEmpty()) {
                    // 이미지 파일 저장 및 URL 가져오기
                    String imageUrl = fileService.saveFile(image, "restaurants");
                    
                    // 이미지 엔티티 생성 및 저장
                    UploadImage uploadImage = new UploadImage();
                    uploadImage.setUpload(upload);
                    uploadImage.setUrl(imageUrl);
                    uploadImageRepository.save(uploadImage);
                }
            }
        }
        
        // 식당 저장
        return uploadRepository.save(upload);
    }
	
	


    public List<Upload> getList() {
        List<Upload> uploads = this.uploadRepository.findAll();
        uploads.forEach(upload -> {
            System.out.println(upload.getCategory());  // 로그로 카테고리 출력 확인
        });
        return uploads;
    }
    
	
    //메인페이지 식당 정보 불러오기 메소드??
    public Upload getUpload(Long id) {  
        Optional<Upload> upload = this.uploadRepository.findById(id);
        if (upload.isPresent()) {
            return upload.get();
        } else {
            throw new DataNotFoundException("upload not found");
        }
    }

	public Upload save(Upload upload) {
		return uploadRepository.save(upload);
		
	}
	
    // updateRating 메서드 추가
    @Transactional
    public void updateRating(Upload upload, List<Comments> comments) {
        if (comments.isEmpty()) {
            upload.setRating(0.0);
        } else {
            double sum = 0;
            int count = 0;
            
            for (Comments comment : comments) {
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
        upload.setReviewCount(comments.size());
        
        // 저장
        uploadRepository.save(upload);
    }
    
    // 식당 이미지 조회 메서드
    public List<UploadImage> getUploadImagesByUpload(Upload upload) {
        return uploadImageRepository.findByUpload(upload);
    }
	
}