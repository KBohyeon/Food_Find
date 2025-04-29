package com.example.foodfight.upload;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.foodfight.comments.Comments;
import com.example.foodfight.user.SiteUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType; 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany; 
import java.util.Set;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

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
    
    private String imagePath; //대표 이미지

    private String createDate; //날짜, 시간
    
    private Double rating; //점수
    
    private Integer reviewCount; //리뷰 갯수

    private String menu1; //메뉴
    private String menu1Price;
    
    private String menu2;
    private String menu2Price;
    
    private String menu3;
    private String menu3Price;
    
    private String openTime; //오픈시간
    
    private String closeTime; //닫는 시간
    
    private String dayOff; //쉬는 날
    
    private String phone; //전화번호
    
    private String tag1; //태그
    
    private String tag2;
    
    @OneToMany(mappedBy = "upload", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UploadImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "upload", cascade = CascadeType.REMOVE) 
    @JsonIgnoreProperties({"upload", "author", "voter", "images"}) // 순환 참조 방지
    private List<Comments> commentsList; 
    
    @ManyToMany
    Set<SiteUser> voter;

    private String modifyDate;//수정 날짜
    
    @ManyToOne
//    @JsonIgnoreProperties({"commentsList", "voter"}) // 순환 참조 방지
    private SiteUser author;

    
}
