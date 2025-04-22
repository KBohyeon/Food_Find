package com.example.foodfight.comments;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.user.SiteUser;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

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

    private LocalDateTime createDate; 
    
    @ManyToOne 
    private Upload upload;  
    
    //다대다 관계 속성 voter관리를 위해 Set사용
    @ManyToMany
    Set<SiteUser> voter;
    
    private Double rating;
    
    @ManyToOne
    private SiteUser author;
    
//    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<CommentImage> images;
}
