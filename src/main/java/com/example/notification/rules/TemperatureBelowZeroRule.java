package com.example.notification.rules;

import com.example.notification.domain.NotificationRule;
import com.example.notification.dto.NotificationContext;

/**
 * 온도가 영하로 떨어지면 알림을 보내는 샘플 규칙
 * 
 * Claude AI가 "날씨가 영하가 되면 알림해줘" 요청으로 생성하는 코드 예시
 */
public class TemperatureBelowZeroRule implements NotificationRule {
    
    @Override
    public boolean shouldNotify(NotificationContext context) {
        if (context.getWeatherData() == null) {
            return false;
        }
        
        Object tempObj = context.getWeatherData().get("temperature");
        if (tempObj == null) {
            return false;
        }
        
        double temperature = ((Number) tempObj).doubleValue();
        
        // 온도가 0도 미만이면 알림
        return temperature < 0.0;
    }
    
    @Override
    public String getMessage(NotificationContext context) {
        double temperature = ((Number) context.getWeatherData().get("temperature")).doubleValue();
        String condition = (String) context.getWeatherData().get("condition");
        
        return String.format(
                "⚠️ 한파 주의! 현재 온도가 %.1f℃로 영하입니다. 날씨: %s",
                temperature,
                condition
        );
    }
}
