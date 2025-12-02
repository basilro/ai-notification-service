package com.example.notification.service;

import com.example.notification.domain.NotificationRule;
import com.example.notification.domain.RuleEntity;
import com.example.notification.dto.CreateRuleRequest;
import com.example.notification.dto.RuleResponse;
import com.example.notification.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 알림 규칙 관리 서비스
 * 
 * 사용자의 자연어 요청을 받아 코드로 변환하고,
 * 컴파일하여 저장 및 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleManagementService {
    
    private final RuleRepository ruleRepository;
    private final ClaudeApiService claudeApiService;
    private final DynamicCodeEngine dynamicCodeEngine;
    
    /**
     * 컴파일된 규칙 인스턴스 캐시
     * Key: Rule ID, Value: NotificationRule 인스턴스
     */
    private final Map<Long, NotificationRule> ruleCache = new ConcurrentHashMap<>();
    
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("public\\s+class\\s+(\\w+)");
    
    /**
     * 새로운 알림 규칙을 생성합니다
     * 
     * 1. Claude API를 통해 코드 생성
     * 2. 동적 컴파일 및 테스트
     * 3. DB에 저장
     * 4. 캐시에 로드
     */
    @Transactional
    public RuleResponse createRule(CreateRuleRequest request) {
        log.info("새로운 규칙 생성 요청: userId={}, request={}", 
                request.getUserId(), request.getRequest());
        
        try {
            // 1. Claude API로 코드 생성
            String generatedCode = claudeApiService.generateRuleCode(request.getRequest());
            log.debug("생성된 코드:\n{}", generatedCode);
            
            // 2. 동적 컴파일 및 테스트
            NotificationRule ruleInstance = dynamicCodeEngine.compileAndLoad(generatedCode);
            
            // 3. 클래스 이름 추출
            String className = extractClassName(generatedCode);
            
            // 4. DB에 저장
            RuleEntity entity = RuleEntity.builder()
                    .userId(request.getUserId())
                    .naturalLanguageRequest(request.getRequest())
                    .generatedCode(generatedCode)
                    .className(className)
                    .active(true)
                    .cronExpression(request.getCronExpression() != null ? 
                            request.getCronExpression() : "0 0/10 * * * ?")
                    .executionCount(0)
                    .notificationCount(0)
                    .build();
            
            entity = ruleRepository.save(entity);
            
            // 5. 캐시에 로드
            ruleCache.put(entity.getId(), ruleInstance);
            
            log.info("규칙 생성 성공: id={}, className={}", entity.getId(), className);
            
            return toResponse(entity);
            
        } catch (Exception e) {
            log.error("규칙 생성 실패", e);
            throw new RuntimeException("규칙 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 특정 사용자의 모든 규칙을 조회합니다
     */
    @Transactional(readOnly = true)
    public List<RuleResponse> getRulesByUserId(String userId) {
        return ruleRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 규칙을 조회합니다
     */
    @Transactional(readOnly = true)
    public RuleResponse getRule(Long ruleId) {
        RuleEntity entity = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("규칙을 찾을 수 없습니다: " + ruleId));
        return toResponse(entity);
    }
    
    /**
     * 규칙을 비활성화합니다
     */
    @Transactional
    public void deactivateRule(Long ruleId) {
        RuleEntity entity = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("규칙을 찾을 수 없습니다: " + ruleId));
        
        entity.setActive(false);
        ruleRepository.save(entity);
        
        // 캐시에서 제거
        ruleCache.remove(ruleId);
        
        log.info("규칙 비활성화: id={}", ruleId);
    }
    
    /**
     * 규칙을 삭제합니다
     */
    @Transactional
    public void deleteRule(Long ruleId) {
        ruleRepository.deleteById(ruleId);
        ruleCache.remove(ruleId);
        log.info("규칙 삭제: id={}", ruleId);
    }
    
    /**
     * 컴파일된 규칙 인스턴스를 가져옵니다
     * 캐시에 없으면 DB에서 로드하여 컴파일합니다
     */
    public NotificationRule getRuleInstance(Long ruleId) {
        return ruleCache.computeIfAbsent(ruleId, id -> {
            RuleEntity entity = ruleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("규칙을 찾을 수 없습니다: " + id));
            
            try {
                return dynamicCodeEngine.compileAndLoad(entity.getGeneratedCode());
            } catch (Exception e) {
                log.error("규칙 로드 실패: id={}", id, e);
                throw new RuntimeException("규칙 로드 실패", e);
            }
        });
    }
    
    /**
     * 모든 활성화된 규칙을 조회합니다
     */
    @Transactional(readOnly = true)
    public List<RuleEntity> getActiveRules() {
        return ruleRepository.findByActiveTrue();
    }
    
    /**
     * Entity를 Response DTO로 변환합니다
     */
    private RuleResponse toResponse(RuleEntity entity) {
        return RuleResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .naturalLanguageRequest(entity.getNaturalLanguageRequest())
                .generatedCode(entity.getGeneratedCode())
                .className(entity.getClassName())
                .active(entity.getActive())
                .cronExpression(entity.getCronExpression())
                .createdAt(entity.getCreatedAt())
                .lastExecutedAt(entity.getLastExecutedAt())
                .executionCount(entity.getExecutionCount())
                .notificationCount(entity.getNotificationCount())
                .build();
    }
    
    /**
     * 소스 코드에서 클래스 이름을 추출합니다
     */
    private String extractClassName(String sourceCode) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("클래스 이름을 찾을 수 없습니다");
    }
}
