package com.example.foodfight.comments;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.user.SiteUser;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.persistence.ManyToMany;
import com.example.foodfight.user.SiteUser;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;
    
    
    private String createDate; 
    
    private String modifyDate;
    //저장할때 처리함 필요없음 아마
//    @PrePersist
//    public void onPrePersist(){
//        this.createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//    }
	
    @ManyToOne 
    @JsonIgnoreProperties("commentsList") // 순환 참조 방지
    private Upload upload;  
    
    //다대다 관계 속성 voter관리를 위해 Set사용
    @ManyToMany
    @JsonIgnoreProperties({"commentsList", "voter"}) // 순환 참조 방지
    Set<SiteUser> voter;
    
    private Double rating;
    
    @ManyToOne
    @JsonIgnoreProperties({"commentsList", "voter"}) // 순환 참조 방지
    private SiteUser author;
    
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("comment") // 순환 참조 방지
    private List<CommentImage> images = new ArrayList<>(); // 초기화 추가
}
