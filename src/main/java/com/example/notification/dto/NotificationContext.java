package com.example.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 알림 규칙 실행 컨텍스트
 * 
 * 외부 API에서 가져온 데이터와 실행 환경 정보를 포함합니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationContext {
    
    /**
     * 날씨 정보
     * temperature: 온도 (섭씨)
     * condition: 날씨 상태 (Clear, Rain, Snow 등)
     */
    private Map<String, Object> weatherData;
    
    /**
     * 주식 정보
     * price: 현재 가격
     * symbol: 종목 코드
     */
    private Map<String, Object> stockData;
    
    /**
     * 뉴스 정보
     * headlines: 뉴스 제목 리스트
     */
    private Map<String, Object> newsData;
    
    /**
     * 사용자 정의 데이터
     */
    private Map<String, Object> customData;
    
    /**
     * 실행 시각 타임스탬프
     */
    private Long timestamp;
}
