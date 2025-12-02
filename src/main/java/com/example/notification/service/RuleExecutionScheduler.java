package com.example.notification.service;

import com.example.notification.domain.NotificationRule;
import com.example.notification.domain.RuleEntity;
import com.example.notification.dto.NotificationContext;
import com.example.notification.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 규칙 실행 스케줄러
 * 
 * 주기적으로 (기본 10분마다) 모든 활성화된 규칙을 실행하고,
 * 조건이 충족되면 알림을 발송합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleExecutionScheduler {
    
    private final RuleManagementService ruleManagementService;
    private final RuleRepository ruleRepository;
    private final ExternalApiService externalApiService;
    private final NotificationService notificationService;
    
    /**
     * 10분마다 모든 활성화된 규칙을 실행합니다
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    @Transactional
    public void executeAllRules() {
        log.info("규칙 실행 스케줄러 시작");
        
        // 1. 외부 API 데이터 수집
        NotificationContext context = externalApiService.fetchContext();
        
        // 2. 모든 활성화된 규칙 조회
        List<RuleEntity> activeRules = ruleManagementService.getActiveRules();
        
        log.info("실행할 규칙 개수: {}", activeRules.size());
        
        // 3. 각 규칙 실행
        for (RuleEntity ruleEntity : activeRules) {
            try {
                executeRule(ruleEntity, context);
            } catch (Exception e) {
                log.error("규칙 실행 중 오류: ruleId={}", ruleEntity.getId(), e);
            }
        }
        
        log.info("규칙 실행 스케줄러 종료");
    }
    
    /**
     * 개별 규칙을 실행합니다
     */
    private void executeRule(RuleEntity ruleEntity, NotificationContext context) {
        log.debug("규칙 실행: ruleId={}, className={}", 
                ruleEntity.getId(), ruleEntity.getClassName());
        
        // 1. 컴파일된 규칙 인스턴스 가져오기
        NotificationRule rule = ruleManagementService.getRuleInstance(ruleEntity.getId());
        
        // 2. 실행 횟수 증가
        ruleEntity.setExecutionCount(ruleEntity.getExecutionCount() + 1);
        ruleEntity.setLastExecutedAt(LocalDateTime.now());
        
        // 3. 알림 조건 평가
        boolean shouldNotify = rule.shouldNotify(context);
        
        if (shouldNotify) {
            // 4. 알림 메시지 생성
            String message = rule.getMessage(context);
            
            log.info("알림 발생: ruleId={}, userId={}, message={}", 
                    ruleEntity.getId(), ruleEntity.getUserId(), message);
            
            // 5. 알림 발송
            notificationService.sendNotification(ruleEntity.getUserId(), message);
            
            // 6. 알림 횟수 증가
            ruleEntity.setNotificationCount(ruleEntity.getNotificationCount() + 1);
        }
        
        // 7. 변경사항 저장
        ruleRepository.save(ruleEntity);
    }
}
