package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 알림 규칙 생성 요청 DTO
 */
@Data
public class CreateRuleRequest {
    
    /**
     * 사용자 ID
     */
    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;
    
    /**
     * 자연어 알림 요청
     * 예: "날씨가 영하가 되면 알림해줘"
     */
    @NotBlank(message = "알림 요청 내용은 필수입니다")
    private String request;
    
    /**
     * 실행 주기 (cron 표현식)
     * 기본값: "0 0/10 * * * ?" (10분마다)
     */
    private String cronExpression;
}
