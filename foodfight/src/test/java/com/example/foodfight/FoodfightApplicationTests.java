package com.example.foodfight;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadRepository;

@SpringBootTest
class FoodfightApplicationTests {
    @Autowired
    private UploadRepository uploadRepository;
    
    @Test
    void testJpa() {        
        Upload q1 = new Upload();
        q1.setSubject("sbb가 무엇인가요?");
        q1.setContent("sbb에 대해서 알고 싶습니다.");
        q1.setCreateDate(LocalDateTime.now());
        this.uploadRepository.save(q1);  // 첫번째 질문 저장

    }
}
