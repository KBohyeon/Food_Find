package com.example.foodfight;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "entity not found")
public class DataNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public DataNotFoundException(String message) {
        super(message);
    }
}

//404 오류 반환 ataNotFoundException 정의 클래스