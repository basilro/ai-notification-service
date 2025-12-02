# 사용 가이드

## 목차
- [빠른 시작](#빠른-시작)
- [알림 규칙 작성 가이드](#알림-규칙-작성-가이드)
- [Cron 표현식 가이드](#cron-표현식-가이드)
- [WebSocket 클라이언트 구현](#websocket-클라이언트-구현)
- [문제 해결](#문제-해결)

## 빠른 시작

### 1. 환경 설정 확인

```bash
# Java 버전 확인 (17 이상 필요)
java -version

# PostgreSQL 실행 확인
psql -U postgres -c "SELECT version();"
```

### 2. 애플리케이션 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 또는 JAR 파일로 실행
java -jar build/libs/ai-notification-service-0.0.1-SNAPSHOT.jar
```

### 3. 첫 번째 알림 규칙 만들기

```bash
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "request": "날씨가 영하가 되면 알림해줘"
  }'
```

성공하면 다음과 같은 응답을 받습니다:
```json
{
  "id": 1,
  "userId": "test-user",
  "naturalLanguageRequest": "날씨가 영하가 되면 알림해줘",
  "className": "TemperatureBelowZeroRule",
  "active": true,
  "cronExpression": "0 0/10 * * * ?"
}
```

## 알림 규칙 작성 가이드

### 효과적인 알림 요청 작성 방법

#### ✅ 좋은 예시

1. **명확한 조건 제시**
```
"날씨가 영하가 되면 알림해줘"
"코스피가 3000을 넘으면 알림해줘"
"온도가 30도 이상이면 알림해줘"
```

2. **구체적인 수치 포함**
```
"비트코인 가격이 50000달러 이하로 떨어지면 알림해줘"
"습도가 70% 이상이면 알림해줘"
```

3. **복합 조건 (AND/OR)**
```
"날씨가 비이고 온도가 10도 이하면 알림해줘"
"코스피가 2800 이하이거나 3200 이상이면 알림해줘"
```

#### ❌ 피해야 할 예시

```
"날씨 좋으면 알림해줘"  // 너무 모호함
"주식 보고 알림해줘"   // 조건이 불명확
"가끔 알림해줘"        // 기준이 없음
```

### 사용 가능한 데이터 필드

#### 날씨 데이터 (weatherData)
```json
{
  "temperature": -5.0,      // 온도 (섭씨)
  "condition": "Clear",     // 날씨 상태
  "humidity": 65,           // 습도 (%)
  "windSpeed": 12.5         // 풍속 (m/s)
}
```

**사용 예시:**
```
"온도가 25도 이상이고 날씨가 맑으면 알림해줘"
"습도가 80% 이상이면 알림해줘"
"풍속이 15m/s 이상이면 알림해줘"
```

#### 주식 데이터 (stockData)
```json
{
  "symbol": "KOSPI",        // 종목 코드
  "price": 3150.25,         // 현재 가격
  "change": 45.50,          // 변동 금액
  "changePercent": 1.47     // 변동률 (%)
}
```

**사용 예시:**
```
"코스피가 3000 이상이면 알림해줘"
"주가 변동률이 5% 이상이면 알림해줘"
"주가가 전일 대비 100포인트 이상 상승하면 알림해줘"
```

#### 뉴스 데이터 (newsData)
```json
{
  "headlines": ["기술주 강세 지속", ...],
  "count": 5
}
```

**사용 예시:**
```
"뉴스 헤드라인에 'AI' 키워드가 있으면 알림해줘"
"기술 관련 뉴스가 3개 이상이면 알림해줘"
```

## Cron 표현식 가이드

Cron 표현식으로 알림 규칙의 실행 주기를 설정할 수 있습니다.

### Cron 표현식 형식

```
초 분 시 일 월 요일
┬  ┬  ┬  ┬  ┬  ┬
│  │  │  │  │  │
│  │  │  │  │  └─ 요일 (0-7, 0과 7은 일요일)
│  │  │  │  └──── 월 (1-12)
│  │  │  └─────── 일 (1-31)
│  │  └────────── 시 (0-23)
│  └───────────── 분 (0-59)
└──────────────── 초 (0-59)
```

### 자주 사용하는 Cron 표현식

| 표현식 | 설명 |
|--------|------|
| `0 0/10 * * * ?` | 10분마다 실행 (기본값) |
| `0 0/5 * * * ?` | 5분마다 실행 |
| `0 0/30 * * * ?` | 30분마다 실행 |
| `0 0 * * * ?` | 매 시각 정각에 실행 |
| `0 0 9 * * ?` | 매일 오전 9시에 실행 |
| `0 0 9,18 * * ?` | 매일 오전 9시, 오후 6시에 실행 |
| `0 0 9 * * MON-FRI` | 평일 오전 9시에 실행 |
| `0 0 0 1 * ?` | 매월 1일 자정에 실행 |

### Cron 표현식 예제

```bash
# 5분마다 체크
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "request": "코스피가 3000을 넘으면 알림해줘",
    "cronExpression": "0 0/5 * * * ?"
  }'

# 평일 오전 9시에만 체크
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "request": "날씨가 영하가 되면 알림해줘",
    "cronExpression": "0 0 9 * * MON-FRI"
  }'
```

## WebSocket 클라이언트 구현

### HTML + JavaScript 예제

```html
<!DOCTYPE html>
<html>
<head>
    <title>AI 알림 서비스</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <h1>실시간 알림</h1>
    <div id="notifications"></div>

    <script>
        const userId = 'user123';
        
        // WebSocket 연결
        const socket = new SockJS('http://localhost:8080/ws');
        const stompClient = Stomp.over(socket);
        
        stompClient.connect({}, function(frame) {
            console.log('WebSocket 연결 성공:', frame);
            
            // 알림 구독
            stompClient.subscribe('/user/queue/notifications', function(message) {
                const notification = message.body;
                displayNotification(notification);
            });
        });
        
        function displayNotification(message) {
            const div = document.getElementById('notifications');
            const p = document.createElement('p');
            const now = new Date().toLocaleTimeString();
            p.textContent = `[${now}] ${message}`;
            div.insertBefore(p, div.firstChild);
        }
    </script>
</body>
</html>
```

### React 예제

```javascript
import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function NotificationComponent({ userId }) {
    const [notifications, setNotifications] = useState([]);
    const [connected, setConnected] = useState(false);

    useEffect(() => {
        // WebSocket 연결
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);

        client.connect({}, (frame) => {
            console.log('Connected:', frame);
            setConnected(true);

            // 알림 구독
            client.subscribe('/user/queue/notifications', (message) => {
                const notification = {
                    text: message.body,
                    timestamp: new Date()
                };
                setNotifications(prev => [notification, ...prev]);
            });
        });

        // 컴포넌트 언마운트 시 연결 해제
        return () => {
            if (client.connected) {
                client.disconnect();
            }
        };
    }, [userId]);

    return (
        <div>
            <h2>실시간 알림</h2>
            <p>연결 상태: {connected ? '✅ 연결됨' : '❌ 연결 안됨'}</p>
            <ul>
                {notifications.map((notif, index) => (
                    <li key={index}>
                        [{notif.timestamp.toLocaleTimeString()}] {notif.text}
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default NotificationComponent;
```

## 문제 해결

### 1. Claude API 오류

**증상:** `Claude API 호출 중 오류 발생`

**해결:**
```bash
# API 키 확인
echo $CLAUDE_API_KEY

# API 키가 없다면 설정
export CLAUDE_API_KEY="your-api-key-here"

# application.yml에서도 확인
cat src/main/resources/application.yml | grep api-key
```

### 2. 컴파일 오류

**증상:** `Java 컴파일러를 찾을 수 없습니다. JDK가 필요합니다`

**해결:**
- JDK (Java Development Kit)가 설치되어 있는지 확인
- JRE가 아닌 JDK 사용 확인
```bash
javac -version  # JDK 설치 확인
```

### 3. 데이터베이스 연결 오류

**증상:** `Could not connect to PostgreSQL`

**해결:**
```bash
# PostgreSQL 실행 확인
psql -U postgres -c "SELECT 1;"

# 데이터베이스 존재 확인
psql -U postgres -c "\l" | grep notification_db

# 없다면 생성
psql -U postgres -c "CREATE DATABASE notification_db;"
```

### 4. WebSocket 연결 실패

**증상:** WebSocket 연결이 안 됨

**해결:**
- CORS 설정 확인
- 방화벽 설정 확인
- 브라우저 콘솔에서 에러 메시지 확인

### 5. 규칙이 실행되지 않음

**증상:** 규칙을 생성했지만 알림이 오지 않음

**확인 사항:**
```bash
# 규칙이 활성화되어 있는지 확인
curl http://localhost:8080/api/rules/1

# 로그 확인
tail -f logs/application.log | grep "규칙 실행"

# 스케줄러가 동작하는지 확인
tail -f logs/application.log | grep "스케줄러"
```

## 고급 사용법

### 복잡한 조건의 규칙

```bash
# 여러 조건을 조합한 규칙
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "request": "날씨가 비이고 온도가 10도 이하이며 습도가 80% 이상이면 알림해줘"
  }'
```

### 시간대별 다른 규칙 적용

```bash
# 출근 시간 규칙 (평일 오전 7-9시)
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "request": "날씨가 비면 알림해줘",
    "cronExpression": "0 0 7-9 * * MON-FRI"
  }'

# 투자 시간 규칙 (평일 오전 9시-오후 3시)
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "request": "코스피가 5% 이상 변동하면 알림해줘",
    "cronExpression": "0 0/30 9-15 * * MON-FRI"
  }'
```

## 다음 단계

- [아키텍처 문서](ARCHITECTURE.md)에서 시스템 설계 이해하기
- [API 문서](../README.md#api-문서)에서 전체 API 스펙 확인하기
- 프로덕션 배포를 위한 보안 설정 강화하기
