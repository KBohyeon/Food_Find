package com.example.foodfight.upload;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import com.example.foodfight.DataNotFoundException;
import com.example.foodfight.comments.Comments;

import jakarta.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class UploadService {

    private final UploadRepository uploadRepository;

    public List<Upload> getList() {
        return this.uploadRepository.findAll();
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
	
}