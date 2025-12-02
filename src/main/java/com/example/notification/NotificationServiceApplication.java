package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI 기반 알림 서비스 메인 애플리케이션
 * 
 * 사용자가 자연어로 요청한 알림 규칙을 Claude AI가 코드로 변환하고
 * 동적으로 컴파일하여 실행하는 서비스입니다.
 */
@SpringBootApplication
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
