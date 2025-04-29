package com.example.foodfight.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UploadImageService {

    private final UploadImageRepository uploadImageRepository;
    
    @Transactional(readOnly = true)
    public List<UploadImage> getImagesByUploadId(Long uploadId) {
        return uploadImageRepository.findByUploadId(uploadId);
    }
    
    @Transactional
    public void deleteImage(Long imageId) {
        uploadImageRepository.deleteById(imageId);
    }
}
