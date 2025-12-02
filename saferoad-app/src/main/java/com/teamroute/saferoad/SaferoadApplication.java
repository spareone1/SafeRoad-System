package com.teamroute.saferoad;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SaferoadApplication {

	@PostConstruct
	public void started() {
		// 서버 시간대를 한국(KST)으로 강제 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(SaferoadApplication.class, args);
	}

}
