package com.example.notification.domain;

import com.example.notification.dto.NotificationContext;

/**
 * 알림 규칙 인터페이스
 * 
 * AI가 생성한 코드는 이 인터페이스를 구현해야 합니다.
 * 각 규칙은 조건을 평가하고, 조건이 충족되면 알림 메시지를 반환합니다.
 */
public interface NotificationRule {
    
    /**
     * 알림 조건을 평가합니다
     * 
     * @param context 외부 API 데이터 및 실행 컨텍스트
     * @return 조건이 충족되면 true, 아니면 false
     */
    boolean shouldNotify(NotificationContext context);
    
    /**
     * 알림 메시지를 생성합니다
     * 
     * @param context 외부 API 데이터 및 실행 컨텍스트
     * @return 사용자에게 전송할 알림 메시지
     */
    String getMessage(NotificationContext context);
}
