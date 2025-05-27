package com.example.foodfight.reservation;


 //예약이 가득 찬 경우 발생하는 예외
 //해당 시간대에 이미 예약이 있을 때 사용
 
public class ReservationFullException extends RuntimeException {
    
    public ReservationFullException(String message) {
        super(message);
    }
    
    public ReservationFullException(String message, Throwable cause) {
        super(message, cause);
    }
}