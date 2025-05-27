package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;
import com.example.foodfight.user.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/owner/reservation")
@Controller
public class OwnerReservationController {

    private final ReservationService reservationService;
    private final UploadService uploadService;
    private final UserService userService;
    
    /**
     * 사장님의 식당 목록 조회
     * 식당 사장님 권한만 접근 가능
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/restaurants")
    public String myRestaurants(Model model, Principal principal) {
        SiteUser user = userService.getUserByUsername(principal.getName());
        List<Upload> restaurants = uploadService.getRestaurantsByOwner(user);
        
        model.addAttribute("restaurants", restaurants);
        return "owner/my_restaurants";
    }
    
    /**
     * 특정 식당의 예약 목록 조회 (날짜 필터 및 통계 추가)
     * 식당 소유자만 접근 가능
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/restaurant/{restaurantId}")
    public String restaurantReservations(@PathVariable("restaurantId") Long restaurantId, 
                                        @RequestParam(value = "date", required = false) 
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate filterDate,
                                        Model model, Principal principal) {
        // 현재 로그인한 사용자
        SiteUser user = userService.getUserByUsername(principal.getName());
        // 식당 정보
        Upload restaurant = uploadService.getUpload(restaurantId);
        
        // 식당 소유자 확인 - 본인의 식당만 조회 가능
        if (!restaurant.getOwner().getId().equals(user.getId())) {
            return "redirect:/owner/reservation/restaurants?error=unauthorized";
        }
        
        // 식당 예약 목록 조회 (날짜 필터 적용)
        List<Reservation> reservations;
        if (filterDate != null) {
            reservations = reservationService.getRestaurantReservationsByDate(restaurant, filterDate);
            model.addAttribute("filterDate", filterDate);
        } else {
            reservations = reservationService.getRestaurantReservations(restaurant);
        }
        
        // 예약을 날짜와 시간 기준으로 정렬 (빠른 날짜/시간 먼저)
        reservations = reservations.stream()
            .sorted(Comparator.comparing(Reservation::getReservationDate)
                    .thenComparing(Reservation::getReservationTime))
            .collect(Collectors.toList());
        
        // 통계 데이터 계산 - 확정된 예약만 집계
        LocalDate today = LocalDate.now();
        int todayCount = (int) reservations.stream()
            .filter(r -> r.getReservationDate().equals(today) && r.getStatus() == ReservationStatus.CONFIRMED)
            .count();
        
        // 이번 주 예약 통계
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        int weekCount = (int) reservations.stream()
            .filter(r -> !r.getReservationDate().isBefore(startOfWeek) && 
                   !r.getReservationDate().isAfter(endOfWeek) && 
                   r.getStatus() == ReservationStatus.CONFIRMED)
            .count();
        
        // 이번 달 예약 통계
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        int monthCount = (int) reservations.stream()
            .filter(r -> !r.getReservationDate().isBefore(startOfMonth) && 
                   !r.getReservationDate().isAfter(endOfMonth) && 
                   r.getStatus() == ReservationStatus.CONFIRMED)
            .count();
        
        // 평균 예약 인원수 계산
        double averagePersons = reservations.stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .mapToInt(Reservation::getPersonCount)
            .average()
            .orElse(0);
        
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("reservations", reservations);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("weekCount", weekCount);
        model.addAttribute("monthCount", monthCount);
        model.addAttribute("averagePersons", String.format("%.1f", averagePersons));
        
        return "owner/restaurant_reservations";
    }
    
    
     //예약 취소 확정기능 삭제
     //손님이 예약시 이미 확정으로 들어가므로 사장님은 취소만 가능
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update-status/{reservationId}")
    public String updateReservationStatus(@PathVariable("reservationId") Long reservationId,
                                         @RequestParam("status") String status,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes) {
        
        try {
            // 현재 로그인한 사용자 (식당 사장님)
            SiteUser user = userService.getUserByUsername(principal.getName());
            // 예약 정보
            Reservation reservation = reservationService.getReservation(reservationId);
            // 식당 정보
            Upload restaurant = reservation.getRestaurant();
            
            // 식당 소유자 확인 - 본인의 식당 예약만 변경 가능
            if (!restaurant.getOwner().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "권한이 없습니다.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/owner/reservation/restaurants";
            }
            
            // 취소 처리만 가능
            if ("CANCELED".equals(status)) {
                if (reservation.getStatus() != ReservationStatus.CANCELED) {
                    // 예약 취소 처리
                    reservationService.cancelReservation(reservation);
                    redirectAttributes.addFlashAttribute("message", "예약이 취소되었습니다.");
                    redirectAttributes.addFlashAttribute("messageType", "warning");
                } else {
                    // 이미 취소된 예약
                    redirectAttributes.addFlashAttribute("error", "이미 취소된 예약입니다.");
                    redirectAttributes.addFlashAttribute("messageType", "info");
                }
            } else {
                // 잘못된 상태값 (CONFIRMED 등은 더 이상 지원하지 않음)
                redirectAttributes.addFlashAttribute("error", "지원하지 않는 작업입니다. 취소만 가능합니다.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
            }
            
            return "redirect:/owner/reservation/restaurant/" + restaurant.getId();
            
        } catch (Exception e) {
            // 일반적인 오류 처리
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "예약 처리 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/owner/reservation/restaurants";
        }
    }
}