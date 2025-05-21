package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UploadService uploadService;
    
    // 예약 생성
    @Transactional
    public Reservation createReservation(Upload restaurant, SiteUser user, 
                                        LocalDate date, LocalTime time, 
                                        Integer personCount, String request) {
        
        Reservation reservation = new Reservation();
        reservation.setRestaurant(restaurant);
        reservation.setUser(user);
        reservation.setReservationDate(date);
        reservation.setReservationTime(time);
        reservation.setPersonCount(personCount);
        reservation.setRequest(request);
        //기본적으로 예약 확정으로 함
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return reservationRepository.save(reservation);
    }
    
    // 예약 조회d
    public Reservation getReservation(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent()) {
            return reservation.get();
        } else {
            throw new RuntimeException("예약 정보를 찾을 수 없습니다.");
        }
    }
    
    // 사용자별 예약 목록 조회
    public List<Reservation> getUserReservations(SiteUser user) {
        return reservationRepository.findByUser(user);
    }
    
    // 식당별 예약 목록 조회
    public List<Reservation> getRestaurantReservations(Upload restaurant) {
        return reservationRepository.findByRestaurant(restaurant);
    }
    
    // 날짜별 식당 예약 조회
    public List<Reservation> getRestaurantReservationsByDate(Upload restaurant, LocalDate date) {
        return reservationRepository.findByRestaurantAndReservationDate(restaurant, date);
    }
    
    // 예약 취소
    @Transactional
    public void cancelReservation(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);
    }
    
    // 예약 수정
    @Transactional
    public Reservation updateReservation(Reservation reservation, 
                                         LocalDate date, LocalTime time, 
                                         Integer personCount, String request) {
        
        reservation.setReservationDate(date);
        reservation.setReservationTime(time);
        reservation.setPersonCount(personCount);
        reservation.setRequest(request);
        
        return reservationRepository.save(reservation);
    }
    
 // 예약 확정
    @Transactional
    public void confirmReservation(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }
}