package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    
    // Redis 분산 잠금을 위한 RedisTemplate 추가
    private final RedisTemplate<String, String> redisTemplate;
    
    
     //예약 생성 - Redis 분산 잠금 적용
     //동시에 같은 시간대 예약을 시도하는 경우 순차적으로 처리
    @Transactional
    public Reservation createReservation(Upload restaurant, SiteUser user, 
                                        LocalDate date, LocalTime time, 
                                        Integer personCount, String request) {
        
        //Redis 분산 잠금 키 생성 (식당ID + 날짜 + 시간)
        String lockKey = String.format("reservation_lock:%d:%s:%s", 
            restaurant.getId(), 
            date.toString(),
            time.toString());
        
        //분산 잠금 획득 시도 (30초 후 만료)
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", Duration.ofSeconds(30));
        
        if (!lockAcquired) {
            //다른 사용자가 예약 처리 중인 경우
            throw new ReservationBusyException("다른 고객이 예약 중입니다. 잠시 후 다시 시도해주세요.");
        }
        
        try {
            //실제 예약 처리 (MySQL 데이터베이스 사용)
            return processReservationWithDatabase(restaurant, user, date, time, personCount, request);
        } 
        
        finally {
            //예약 처리 완료 후 잠금 해제
            redisTemplate.delete(lockKey);
        }
    }
    
    
     //데이터베이스를 이용한 실제 예약 처리(mysql)
     //Redis 잠금 내에서 실행되는 메소드
    private Reservation processReservationWithDatabase(Upload restaurant, SiteUser user, 
                                                      LocalDate date, LocalTime time, 
                                                      Integer personCount, String request) {
        
        //해당 날짜/시간에 이미 예약이 있는지 확인
        List<Reservation> existingReservations = reservationRepository
            .findByRestaurantAndReservationDateAndReservationTime(restaurant, date, time);
        
        //확정된 예약이 이미 있는지 검사
        boolean alreadyBooked = existingReservations.stream()
            .anyMatch(r -> r.getStatus() == ReservationStatus.CONFIRMED);
        
        if (alreadyBooked) {
            throw new ReservationFullException("해당 시간대는 이미 예약이 완료되었습니다.");
        }
        
        //새 예약 생성
        Reservation reservation = new Reservation();
        reservation.setRestaurant(restaurant);
        reservation.setUser(user);
        reservation.setReservationDate(date);
        reservation.setReservationTime(time);
        reservation.setPersonCount(personCount);
        reservation.setRequest(request);
        reservation.setStatus(ReservationStatus.CONFIRMED); // 기본적으로 예약 확정
        reservation.setCreateDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        //데이터베이스에 저장
        return reservationRepository.save(reservation);
    }
    

     //예약 조회
    public Reservation getReservation(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent()) {
            return reservation.get();
        } else {
            throw new RuntimeException("예약 정보를 찾을 수 없습니다.");
        }
    }
    
    
    //사용자별 예약 목록 조회
    public List<Reservation> getUserReservations(SiteUser user) {
        return reservationRepository.findByUser(user);
    }
    
    //식당별 예약 목록 조회
    public List<Reservation> getRestaurantReservations(Upload restaurant) {
        return reservationRepository.findByRestaurant(restaurant);
    }
    
    //날짜별 식당 예약 조회 
    public List<Reservation> getRestaurantReservationsByDate(Upload restaurant, LocalDate date) {
        return reservationRepository.findByRestaurantAndReservationDate(restaurant, date);
    }
    
    //예약취소
    @Transactional
    public void cancelReservation(Reservation reservation) {
        
        //이미 취소된 예약인지 확인 후 이미 취소 되어 있으면 그냥 반환
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return;
        }
        
        //예약 상태를 취소로 변경
        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);
    }
}