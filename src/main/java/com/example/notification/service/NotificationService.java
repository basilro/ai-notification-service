package com.example.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 알림 발송 서비스
 * 
 * WebSocket, Email, Push, SMS 등 다양한 채널로 알림을 발송합니다.
 * 데모 버전에서는 WebSocket만 구현합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 사용자에게 알림을 발송합니다
     * 
     * @param userId 사용자 ID
     * @param message 알림 메시지
     */
    public void sendNotification(String userId, String message) {
        log.info("알림 발송: userId={}, message={}", userId, message);
        
        try {
            // WebSocket을 통한 실시간 알림
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notifications",
                    message
            );
            
            log.debug("WebSocket 알림 발송 성공: userId={}", userId);
            
        } catch (Exception e) {
            log.error("알림 발송 실패: userId={}", userId, e);
        }
        
        // TODO: Email, Push, SMS 발송 기능 추가
    }
}
