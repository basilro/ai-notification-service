package com.example.notification.service;

import com.example.notification.dto.NotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 외부 API 통합 서비스
 * 
 * 날씨, 주식, 뉴스 등의 외부 API로부터 데이터를 가져옵니다.
 * 데모 버전에서는 목데이터를 반환합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {
    
    private final Random random = new Random();
    
    /**
     * 모든 외부 API 데이터를 가져와서 NotificationContext를 구성합니다
     */
    public NotificationContext fetchContext() {
        return NotificationContext.builder()
                .weatherData(fetchWeatherData())
                .stockData(fetchStockData())
                .newsData(fetchNewsData())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 날씨 데이터 조회 (데모: 목데이터)
     * 
     * 실제 구현시 OpenWeatherMap API 등을 통합
     */
    private Map<String, Object> fetchWeatherData() {
        Map<String, Object> weatherData = new HashMap<>();
        
        // -10℃ ~ 35℃ 범위의 난수 온도
        double temperature = -10 + (random.nextDouble() * 45);
        
        String[] conditions = {"Clear", "Clouds", "Rain", "Snow"};
        String condition = conditions[random.nextInt(conditions.length)];
        
        weatherData.put("temperature", Math.round(temperature * 10.0) / 10.0);
        weatherData.put("condition", condition);
        weatherData.put("humidity", 50 + random.nextInt(50));
        weatherData.put("windSpeed", random.nextDouble() * 20);
        
        log.debug("날씨 데이터 조회: {}", weatherData);
        return weatherData;
    }
    
    /**
     * 주식 데이터 조회 (데모: 목데이터)
     * 
     * 실제 구현시 국내 증권 API 또는 Yahoo Finance API 등을 통합
     */
    private Map<String, Object> fetchStockData() {
        Map<String, Object> stockData = new HashMap<>();
        
        // KOSPI 지수 2500 ~ 3200 범위
        double kospiPrice = 2500 + (random.nextDouble() * 700);
        
        stockData.put("symbol", "KOSPI");
        stockData.put("price", Math.round(kospiPrice * 100.0) / 100.0);
        stockData.put("change", Math.round((random.nextDouble() * 100 - 50) * 100.0) / 100.0);
        stockData.put("changePercent", Math.round((random.nextDouble() * 4 - 2) * 100.0) / 100.0);
        
        log.debug("주식 데이터 조회: {}", stockData);
        return stockData;
    }
    
    /**
     * 뉴스 데이터 조회 (데모: 목데이터)
     * 
     * 실제 구현시 News API 등을 통합
     */
    private Map<String, Object> fetchNewsData() {
        Map<String, Object> newsData = new HashMap<>();
        
        String[] headlines = {
                "기술주 강세 지속",
                "원자재 가격 상승 우려",
                "AI 산업 성장세 두드러져"
        };
        
        newsData.put("headlines", headlines);
        newsData.put("count", headlines.length);
        
        log.debug("뉴스 데이터 조회: {}", newsData);
        return newsData;
    }
}
