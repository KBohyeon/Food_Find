package com.example.foodfight.upload;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import com.example.foodfight.DataNotFoundException;

@RequiredArgsConstructor
@Service
public class UploadService {

    private final UploadRepository uploadRepository;

    public List<Upload> getList() {
        return this.uploadRepository.findAll();
    }
    
    //제목 눌렀을 때 정보 불러오기 메소드
    public Upload getUpload(Integer id) {  
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
}