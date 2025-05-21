package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.user.SiteUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 예약 일자
    private LocalDate reservationDate;
    
    // 예약 시간
    private LocalTime reservationTime;
    
    // 예약 인원
    private Integer personCount;
    
    // 예약 요청사항
    @Column(columnDefinition = "TEXT")
    private String request;
    
    // 예약 상태 (PENDING, CONFIRMED, CANCELED)d
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    
    // 예약 생성 시간
    private String createDate;
    
    // 예약 식당
    @ManyToOne
    @JsonIgnoreProperties({"reservations"})
    private Upload restaurant;
    
    // 예약자
    @ManyToOne
    @JsonIgnoreProperties({"reservations"})
    private SiteUser user;
}