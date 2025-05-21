package com.example.foodfight.reservation;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;
import com.example.foodfight.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/reservation")
@Controller
public class ReservationController {

    private final ReservationService reservationService;
    private final UploadService uploadService;
    private final UserService userService;
    
    // 예약 페이지 표시d
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create/{restaurantId}")
    public String showReservationForm(@PathVariable("restaurantId") Long restaurantId, Model model) {
        Upload restaurant = uploadService.getUpload(restaurantId);
        
        // 예약이 가능한지 확인 (영업 시간이 설정되어 있는지)
        if (restaurant.getOpenTime() == null || restaurant.getOpenTime().isEmpty() || 
            restaurant.getCloseTime() == null || restaurant.getCloseTime().isEmpty()) {
            // 영업 시간이 없는 경우 오류 메시지와 함께 식당 상세 페이지로 리다이렉트
            return "redirect:/upload/detail/" + restaurantId + "?error=no_reservation";
        }
        
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("today", LocalDate.now());
        return "reservation_form";
    }
    
    // 예약 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{restaurantId}")
    public String createReservation(@PathVariable("restaurantId") Long restaurantId,
                                   @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                                   @RequestParam("personCount") Integer personCount,
                                   @RequestParam(value = "request", required = false) String request,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        
        Upload restaurant = uploadService.getUpload(restaurantId);
        SiteUser user = userService.getUserByUsername(principal.getName());
        
        Reservation reservation = reservationService.createReservation(restaurant, user, date, time, personCount, request);
        
        redirectAttributes.addFlashAttribute("message", "예약이 완료되었습니다.");
        return "redirect:/reservation/my";
    }
    
    // 내 예약 목록 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public String myReservations(Model model, Principal principal) {
        SiteUser user = userService.getUserByUsername(principal.getName());
        List<Reservation> reservations = reservationService.getUserReservations(user);
        
        model.addAttribute("reservations", reservations);
        return "my_reservations";
    }
    
    // 예약 취소
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel/{id}")
    public String cancelReservation(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationService.getReservation(id);
        SiteUser user = userService.getUserByUsername(principal.getName());
        
        // 본인 예약만 취소 가능
        if (!reservation.getUser().getUsername().equals(user.getUsername())) {
            return "redirect:/reservation/my?error=unauthorized";
        }
        
        reservationService.cancelReservation(reservation);
        
        redirectAttributes.addFlashAttribute("message", "예약이 취소되었습니다.");
        return "redirect:/reservation/my";
    }
}