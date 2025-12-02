package com.example.notification.service;

import com.example.notification.config.ClaudeConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Claude API 통합 서비스
 * 
 * 자연어 요청을 Claude AI에 전달하고, Java 코드를 생성받습니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeApiService {
    
    private final ClaudeConfig claudeConfig;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    /**
     * 자연어 요청을 받아 NotificationRule 인터페이스를 구현하는 Java 코드를 생성합니다
     * 
     * @param naturalLanguageRequest 사용자의 자연어 요청
     * @return 생성된 Java 소스 코드
     */
    public String generateRuleCode(String naturalLanguageRequest) {
        log.info("자연어 요청으로부터 코드 생성 시작: {}", naturalLanguageRequest);
        
        String prompt = buildPrompt(naturalLanguageRequest);
        
        WebClient webClient = webClientBuilder
                .baseUrl(claudeConfig.getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", claudeConfig.getKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", claudeConfig.getModel());
        requestBody.put("max_tokens", claudeConfig.getMaxTokens());
        requestBody.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));
        
        try {
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            String generatedCode = extractCodeFromResponse(response);
            log.info("코드 생성 성공: {} 바이트", generatedCode.length());
            
            return generatedCode;
            
        } catch (Exception e) {
            log.error("Claude API 호출 중 오류 발생", e);
            throw new RuntimeException("코드 생성 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * Claude API에 전달할 프롬프트를 구성합니다
     */
    private String buildPrompt(String naturalLanguageRequest) {
        return String.format("""
                다음 자연어 요청에 대한 알림 규칙을 Java 코드로 작성해주세요:
                
                요청: "%s"
                
                요구사항:
                1. com.example.notification.domain.NotificationRule 인터페이스를 구현하는 클래스를 작성해주세요
                2. 클래스 이름은 고유해야 하며, 의미있는 이름을 사용해주세요 (예: TemperatureBelowZeroRule)
                3. shouldNotify() 메서드에서 context의 weatherData, stockData, newsData를 활용하세요
                4. getMessage() 메서드에서 의미있는 알림 메시지를 반환해주세요
                5. package 선언은 package com.example.notification.rules; 를 사용해주세요
                6. import 문을 포함한 완전한 코드를 작성해주세요
                7. 코드 블록에만 사용할 수 있는 안전한 Java 코드를 작성해주세요 (파일 I/O, 네트워크 접속 금지)
                
                컨텍스트 데이터 예시:
                - weatherData: {"temperature": -5.0, "condition": "Clear"}
                - stockData: {"symbol": "KOSPI", "price": 3100.5}
                - newsData: {"headlines": ["...", "..."]}
                
                코드만 반환해주세요 (다른 설명 없이).
                """, naturalLanguageRequest);
    }
    
    /**
     * Claude API 응답에서 생성된 코드를 추출합니다
     */
    private String extractCodeFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("content").get(0);
            String text = content.path("text").asText();
            
            // ```java ... ``` 형식에서 코드 부분만 추출
            if (text.contains("```java")) {
                int startIndex = text.indexOf("```java") + 7;
                int endIndex = text.lastIndexOf("```");
                if (endIndex > startIndex) {
                    return text.substring(startIndex, endIndex).trim();
                }
            } else if (text.contains("```")) {
                int startIndex = text.indexOf("```") + 3;
                int endIndex = text.lastIndexOf("```");
                if (endIndex > startIndex) {
                    return text.substring(startIndex, endIndex).trim();
                }
            }
            
            // 코드 블록이 없으면 전체 텍스트를 반환
            return text.trim();
            
        } catch (Exception e) {
            log.error("응답에서 코드 추출 중 오류 발생", e);
            throw new RuntimeException("코드 추출 실패", e);
        }
    }
}
