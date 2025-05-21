package com.example.foodfight.reservation;
//d
public enum ReservationStatus {
    PENDING("대기중"),
    CONFIRMED("확정됨"),
    CANCELED("취소됨");
    
    private final String displayName;
    
    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}