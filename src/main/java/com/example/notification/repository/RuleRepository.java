package com.example.notification.repository;

import com.example.notification.domain.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 알림 규칙 Repository
 */
@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {
    
    /**
     * 특정 사용자의 모든 규칙 조회
     */
    List<RuleEntity> findByUserId(String userId);
    
    /**
     * 활성화된 모든 규칙 조회
     */
    List<RuleEntity> findByActiveTrue();
    
    /**
     * 특정 사용자의 활성화된 규칙 조회
     */
    List<RuleEntity> findByUserIdAndActiveTrue(String userId);
}
