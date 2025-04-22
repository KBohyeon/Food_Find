package com.example.foodfight.upload;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Set;

import com.example.foodfight.comments.Comments;
import com.example.foodfight.user.SiteUser;

import jakarta.persistence.CascadeType; 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany; 
import java.util.Set;
import jakarta.persistence.ManyToMany;
import com.example.foodfight.user.SiteUser;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String subject; //식당 이름

    @Column(columnDefinition = "TEXT") //상세 설명
    private String content;
    
    private String category; // 카테고리 (한식, 중식, 일식 등)
    
    private String location; // 위치 정보
    
    @Column(length = 500)
    private String imagePath; //사진

    private LocalDateTime createDate; //날짜, 시간
    
    private Double rating; //점수
    
    private Integer reviewCount; //리뷰 갯수
    
    @OneToMany(mappedBy = "upload", cascade = CascadeType.REMOVE) 
    private List<Comments> commentsList; 
    
    @ManyToMany
    Set<SiteUser> voter;
    
}
