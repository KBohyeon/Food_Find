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
    
    // 사장님의 식당 목록 조회d
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/restaurants")
    public String myRestaurants(Model model, Principal principal) {
        SiteUser user = userService.getUserByUsername(principal.getName());
        List<Upload> restaurants = uploadService.getRestaurantsByOwner(user);
        
        model.addAttribute("restaurants", restaurants);
        return "owner/my_restaurants";
    }
    
 // 특정 식당의 예약 목록 조회 (날짜 필터 및 통계 추가)
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
        
        // 식당 소유자 확인
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
        
        // 통계 데이터 계산
        LocalDate today = LocalDate.now();
        int todayCount = (int) reservations.stream()
            .filter(r -> r.getReservationDate().equals(today) && r.getStatus() == ReservationStatus.CONFIRMED)
            .count();
        
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        int weekCount = (int) reservations.stream()
            .filter(r -> !r.getReservationDate().isBefore(startOfWeek) && 
                   !r.getReservationDate().isAfter(endOfWeek) && 
                   r.getStatus() == ReservationStatus.CONFIRMED)
            .count();
        
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        int monthCount = (int) reservations.stream()
            .filter(r -> !r.getReservationDate().isBefore(startOfMonth) && 
                   !r.getReservationDate().isAfter(endOfMonth) && 
                   r.getStatus() == ReservationStatus.CONFIRMED)
            .count();
        
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
    
    // 예약 상태 변경 (확정/취소)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update-status/{reservationId}")
    public String updateReservationStatus(@PathVariable("reservationId") Long reservationId,
                                         @RequestParam("status") String status,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes) {
        // 현재 로그인한 사용자
        SiteUser user = userService.getUserByUsername(principal.getName());
        // 예약 정보
        Reservation reservation = reservationService.getReservation(reservationId);
        // 식당 정보
        Upload restaurant = reservation.getRestaurant();
        
        // 식당 소유자 확인
        if (!restaurant.getOwner().getId().equals(user.getId())) {
            return "redirect:/owner/reservation/restaurants?error=unauthorized";
        }
        
        // 예약 상태 변경
        if ("CONFIRMED".equals(status)) {
            reservationService.confirmReservation(reservation);
            redirectAttributes.addFlashAttribute("message", "예약이 확정되었습니다.");
        } else if ("CANCELED".equals(status)) {
            reservationService.cancelReservation(reservation);
            redirectAttributes.addFlashAttribute("message", "예약이 취소되었습니다.");
        }
        
        return "redirect:/owner/reservation/restaurant/" + restaurant.getId();
    }
}