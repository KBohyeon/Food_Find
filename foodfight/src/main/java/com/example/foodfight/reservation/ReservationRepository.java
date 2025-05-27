package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    /**
     * 사용자별 예약 조회
     */
    List<Reservation> findByUser(SiteUser user);
    
    /**
     * 식당별 예약 조회
     */
    List<Reservation> findByRestaurant(Upload restaurant);
    
    /**
     * 날짜별 식당 예약 조회
     */
    List<Reservation> findByRestaurantAndReservationDate(Upload restaurant, LocalDate date);
    
    /**
     * 사용자 & 상태별 예약 조회
     */
    List<Reservation> findByUserAndStatus(SiteUser user, ReservationStatus status);
    
    /**
     * Redis 분산 잠금 적용을 위한 새로운 메소드
     * 특정 식당의 특정 날짜/시간 예약 조회 (동시성 체크용)
     */
    List<Reservation> findByRestaurantAndReservationDateAndReservationTime(
        Upload restaurant, LocalDate date, LocalTime time);
    
    /**
     * 특정 시간대 확정된 예약 개수 조회
     */
    long countByRestaurantAndReservationDateAndReservationTimeAndStatus(
        Upload restaurant, LocalDate date, LocalTime time, ReservationStatus status);
}