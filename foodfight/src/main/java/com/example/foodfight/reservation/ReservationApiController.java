package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;
import com.example.foodfight.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;
    private final UploadService uploadService;
    private final UserService userService;
    
    // 예약 생성 API
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{restaurantId}")
    public ResponseEntity<?> createReservation(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam("personCount") Integer personCount,
            @RequestParam(value = "request", required = false) String request) {
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            SiteUser user = userService.getUserByUsername(authentication.getName());
            
            // 식당 정보 가져오기
            Upload restaurant = uploadService.getUpload(restaurantId);
            
            // 예약 생성
            Reservation reservation = reservationService.createReservation(restaurant, user, date, time, personCount, request);
            
            // 응답 데이터 생성
            Map<String, Object> response = new HashMap<>();
            response.put("id", reservation.getId());
            response.put("status", "success");
            response.put("message", "예약이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "예약 처리 중 오류가 발생했습니다."));
        }
    }
    
    // 내 예약 목록 조회 API
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations() {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            SiteUser user = userService.getUserByUsername(authentication.getName());
            
            // 예약 목록 조회
            List<Reservation> reservations = reservationService.getUserReservations(user);
            
            // DTO로 변환하여 순환 참조 방지
            List<Map<String, Object>> result = new ArrayList<>();
            for (Reservation reservation : reservations) {
                Map<String, Object> reservationMap = new HashMap<>();
                reservationMap.put("id", reservation.getId());
                reservationMap.put("date", reservation.getReservationDate());
                reservationMap.put("time", reservation.getReservationTime());
                reservationMap.put("personCount", reservation.getPersonCount());
                reservationMap.put("request", reservation.getRequest());
                reservationMap.put("status", reservation.getStatus().name());
                reservationMap.put("statusDisplayName", reservation.getStatus().getDisplayName());
                reservationMap.put("createDate", reservation.getCreateDate());
                
                // 식당 정보 추가
                Map<String, Object> restaurantMap = new HashMap<>();
                restaurantMap.put("id", reservation.getRestaurant().getId());
                restaurantMap.put("name", reservation.getRestaurant().getSubject());
                restaurantMap.put("location", reservation.getRestaurant().getLocation());
                restaurantMap.put("image", reservation.getRestaurant().getImagePath());
                
                reservationMap.put("restaurant", restaurantMap);
                result.add(reservationMap);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "예약 목록 조회 중 오류가 발생했습니다."));
        }
    }
    
    // 예약 취소 API
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable("id") Long id) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            SiteUser user = userService.getUserByUsername(authentication.getName());
            
            // 예약 정보 가져오기d
            Reservation reservation = reservationService.getReservation(id);
            
            // 본인 예약만 취소 가능
            if (!reservation.getUser().getUsername().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "error", "message", "본인의 예약만 취소할 수 있습니다."));
            }
            
            // 이미 취소된 예약인지 확인
            if (reservation.getStatus() == ReservationStatus.CANCELED) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "이미 취소된 예약입니다."));
            }
            
            // 예약 취소
            reservationService.cancelReservation(reservation);
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "예약이 취소되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "예약 취소 중 오류가 발생했습니다."));
        }
    }
    
    // 식당별 예약 가능 시간 조회 API
    @GetMapping("/available-times/{restaurantId}")
    public ResponseEntity<?> getAvailableTimes(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        try {
            Upload restaurant = uploadService.getUpload(restaurantId);
            
            // 해당 날짜의 예약 목록 조회
            List<Reservation> existingReservations = reservationService.getRestaurantReservationsByDate(restaurant, date);
            
            // 예약 가능한 시간대 생성 (예: 영업 시간 내 30분 단위)
            // 식당의 영업 시간 정보 가져오기
            LocalTime openTime = LocalTime.parse(restaurant.getOpenTime());
            LocalTime closeTime = LocalTime.parse(restaurant.getCloseTime());
            
         // 30분 단위로 슬롯 생성
            List<Map<String, Object>> availableSlots = new ArrayList<>();
            LocalTime currentSlot = openTime;

            while (currentSlot.isBefore(closeTime)) {
                // 현재 시간 슬롯을 final 변수에 복사
                final LocalTime timeSlot = currentSlot;
                
                // 이미 예약된 시간인지 확인 (복사한 final 변수 사용)
                // PENDING 상태는 더 이상 확인할 필요 없음 (모두 CONFIRMED로 생성됨)
                boolean isBooked = existingReservations.stream()
                        .anyMatch(r -> r.getReservationTime().equals(timeSlot) && 
                                r.getStatus() == ReservationStatus.CONFIRMED);
                
                if (!isBooked) {
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("time", timeSlot.toString());
                    slot.put("available", true);
                    availableSlots.add(slot);
                }
                
                // 다음 슬롯 (30분 추가)
                currentSlot = currentSlot.plusMinutes(30);
            }
            
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "예약 가능 시간 조회 중 오류가 발생했습니다."));
        }
    }
}