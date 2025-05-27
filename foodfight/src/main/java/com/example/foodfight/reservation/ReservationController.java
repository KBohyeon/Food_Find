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
    
    /**
     * 예약 페이지 표시
     */
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
    
    /**
     * 예약 생성 - Redis 분산 잠금 적용으로 동시성 문제 해결
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{restaurantId}")
    public String createReservation(@PathVariable("restaurantId") Long restaurantId,
                                   @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                                   @RequestParam("personCount") Integer personCount,
                                   @RequestParam(value = "request", required = false) String request,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        
        try {
            Upload restaurant = uploadService.getUpload(restaurantId);
            SiteUser user = userService.getUserByUsername(principal.getName());
            
            // Redis 분산 잠금이 적용된 예약 생성
            Reservation reservation = reservationService.createReservation(restaurant, user, date, time, personCount, request);
            
            redirectAttributes.addFlashAttribute("message", "예약이 완료되었습니다.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/reservation/my";
            
        } catch (ReservationBusyException e) {
            // 다른 사용자가 예약 중인 경우
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/reservation/create/" + restaurantId;
            
        } catch (ReservationFullException e) {
            // 해당 시간대가 이미 예약된 경우
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/reservation/create/" + restaurantId;
            
        } catch (Exception e) {
            // 기타 예상치 못한 오류
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "예약 처리 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/reservation/create/" + restaurantId;
        }
    }
    
    /**
     * 내 예약 목록 조회
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public String myReservations(Model model, Principal principal) {
        SiteUser user = userService.getUserByUsername(principal.getName());
        List<Reservation> reservations = reservationService.getUserReservations(user);
        
        model.addAttribute("reservations", reservations);
        return "my_reservations";
    }
    
    
     //예약 취소
     
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel/{id}")
    public String cancelReservation(@PathVariable("id") Long id,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        try {
            Reservation reservation = reservationService.getReservation(id);
            SiteUser user = userService.getUserByUsername(principal.getName());

            // 본인 예약만 취소 가능
            if (!reservation.getUser().getUsername().equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "본인의 예약만 취소할 수 있습니다.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/reservation/my";
            }

            // 이미 취소된 예약인지 확인
            if (reservation.getStatus() == ReservationStatus.CANCELED) {
                redirectAttributes.addFlashAttribute("error", "이미 취소된 예약입니다.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/reservation/my";
            }

            reservationService.cancelReservation(reservation);

            redirectAttributes.addFlashAttribute("message", "예약이 취소되었습니다.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/reservation/my";

        } catch (Exception e) {
            // 일반적인 오류 처리
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "예약 취소 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/reservation/my";
        }
    }
}