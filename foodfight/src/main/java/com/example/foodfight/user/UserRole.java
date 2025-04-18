package com.example.foodfight.user;
import lombok.Getter;

@Getter
public enum UserRole { //enum(열거 자료형) ADMIN, USER 상수 만든후 ROLE_ADMIN, RPLE_USER 값 부여 상수변경 할 필요 없음 @Setter @Getter만 사용가능
	ADMIN("ROLE_ADMIN"), USER("ROLE_USER");
	
	UserRole(String value) {
		this.value = value;
	}
	
	private String value;
}
