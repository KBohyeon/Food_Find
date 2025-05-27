package com.example.foodfight.reservation;

/**
 * Redis 분산 잠금 획득 실패 시 발생하는 예외
 * 다른 사용자가 동시에 예약을 시도할 때 사용
 */
public class ReservationBusyException extends RuntimeException {
    
    public ReservationBusyException(String message) {
        super(message);
    }
    
    public ReservationBusyException(String message, Throwable cause) {
        super(message, cause);
    }
}