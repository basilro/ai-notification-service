package com.example.notification.rules;

import com.example.notification.domain.NotificationRule;
import com.example.notification.dto.NotificationContext;

/**
 * 주가가 특정 값을 초과하면 알림을 보내는 샘플 규칙
 * 
 * Claude AI가 "코스피가 3000을 넘으면 알림해줘" 요청으로 생성하는 코드 예시
 */
public class StockPriceAboveThresholdRule implements NotificationRule {
    
    private static final double THRESHOLD = 3000.0;
    
    @Override
    public boolean shouldNotify(NotificationContext context) {
        if (context.getStockData() == null) {
            return false;
        }
        
        Object priceObj = context.getStockData().get("price");
        if (priceObj == null) {
            return false;
        }
        
        double price = ((Number) priceObj).doubleValue();
        
        // 주가가 기준값을 초과하면 알림
        return price > THRESHOLD;
    }
    
    @Override
    public String getMessage(NotificationContext context) {
        double price = ((Number) context.getStockData().get("price")).doubleValue();
        String symbol = (String) context.getStockData().get("symbol");
        
        return String.format(
                "📈 %s 지수가 %.2f로 %.0f를 돌파했습니다!",
                symbol,
                price,
                THRESHOLD
        );
    }
}
