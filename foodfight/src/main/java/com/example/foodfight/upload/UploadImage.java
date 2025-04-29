package com.example.foodfight.upload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UploadImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    @JsonIgnoreProperties({"images", "commentsList", "author"}) // 순환 참조 방지
    private Upload upload;
    
    private String url;
}
