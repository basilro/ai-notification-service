package com.example.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 규칙 엔티티
 * 
 * 사용자가 생성한 알림 규칙 정보를 저장합니다.
 */
@Entity
@Table(name = "notification_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 규칙을 생성한 사용자 ID
     */
    @Column(nullable = false)
    private String userId;
    
    /**
     * 사용자가 입력한 자연어 요청
     * 예: "날씨가 영하가 되면 알림해줘"
     */
    @Column(nullable = false, length = 1000)
    private String naturalLanguageRequest;
    
    /**
     * Claude AI가 생성한 Java 소스 코드
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String generatedCode;
    
    /**
     * 컴파일된 클래스 이름
     */
    @Column(nullable = false)
    private String className;
    
    /**
     * 규칙이 활성화 상태인지 여부
     */
    @Column(nullable = false)
    private Boolean active;
    
    /**
     * 실행 주기 (cron 표현식)
     * 예: "0 0/10 * * * ?" (10분마다 실행)
     */
    @Column(nullable = false)
    private String cronExpression;
    
    /**
     * 규칙 생성 시각
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 마지막 수정 시각
     */
    private LocalDateTime updatedAt;
    
    /**
     * 마지막 실행 시각
     */
    private LocalDateTime lastExecutedAt;
    
    /**
     * 총 실행 횟수
     */
    @Column(nullable = false)
    private Integer executionCount;
    
    /**
     * 알림이 발생한 횟수
     */
    @Column(nullable = false)
    private Integer notificationCount;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (executionCount == null) {
            executionCount = 0;
        }
        if (notificationCount == null) {
            notificationCount = 0;
        }
        if (active == null) {
            active = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
