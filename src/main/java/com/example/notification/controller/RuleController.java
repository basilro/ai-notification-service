package com.example.notification.controller;

import com.example.notification.dto.CreateRuleRequest;
import com.example.notification.dto.RuleResponse;
import com.example.notification.service.RuleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 알림 규칙 REST API 컴트롤러
 */
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Slf4j
public class RuleController {
    
    private final RuleManagementService ruleManagementService;
    
    /**
     * 새로운 알림 규칙 생성
     * 
     * POST /api/rules
     * Body: {
     *   "userId": "user123",
     *   "request": "날씨가 영하가 되면 알림해줘",
     *   "cronExpression": "0 0/10 * * * ?"
     * }
     */
    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody CreateRuleRequest request) {
        log.info("규칙 생성 API 호출: userId={}", request.getUserId());
        RuleResponse response = ruleManagementService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 특정 사용자의 모든 규칙 조회
     * 
     * GET /api/rules?userId=user123
     */
    @GetMapping
    public ResponseEntity<List<RuleResponse>> getRulesByUserId(@RequestParam String userId) {
        log.info("사용자 규칙 조회: userId={}", userId);
        List<RuleResponse> rules = ruleManagementService.getRulesByUserId(userId);
        return ResponseEntity.ok(rules);
    }
    
    /**
     * 특정 규칙 조회
     * 
     * GET /api/rules/{ruleId}
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<RuleResponse> getRule(@PathVariable Long ruleId) {
        log.info("규칙 조회: ruleId={}", ruleId);
        RuleResponse rule = ruleManagementService.getRule(ruleId);
        return ResponseEntity.ok(rule);
    }
    
    /**
     * 규칙 비활성화
     * 
     * PATCH /api/rules/{ruleId}/deactivate
     */
    @PatchMapping("/{ruleId}/deactivate")
    public ResponseEntity<Void> deactivateRule(@PathVariable Long ruleId) {
        log.info("규칙 비활성화: ruleId={}", ruleId);
        ruleManagementService.deactivateRule(ruleId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 규칙 삭제
     * 
     * DELETE /api/rules/{ruleId}
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
        log.info("규칙 삭제: ruleId={}", ruleId);
        ruleManagementService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}
