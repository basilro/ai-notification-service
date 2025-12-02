package com.example.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 규칙 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {
    private Long id;
    private String userId;
    private String naturalLanguageRequest;
    private String generatedCode;
    private String className;
    private Boolean active;
    private String cronExpression;
    private LocalDateTime createdAt;
    private LocalDateTime lastExecutedAt;
    private Integer executionCount;
    private Integer notificationCount;
}
