package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 사용자별 예약 조회d
    List<Reservation> findByUser(SiteUser user);
    
    // 식당별 예약 조회
    List<Reservation> findByRestaurant(Upload restaurant);
    
    // 날짜별 식당 예약 조회
    List<Reservation> findByRestaurantAndReservationDate(Upload restaurant, LocalDate date);
    
    // 사용자 & 상태별 예약 조회
    List<Reservation> findByUserAndStatus(SiteUser user, ReservationStatus status);
}