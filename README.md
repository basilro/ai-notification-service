# AI 기반 알림 서비스 (AI Notification Service)

[![Java](https://img.shields.io/badge/Java-17-007396?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Claude API](https://img.shields.io/badge/Claude-API-7C3AED)](https://www.anthropic.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

사용자가 자연어로 요청한 알림 규칙을 Claude AI가 Java 코드로 변환하고, 동적으로 컴파일하여 실행하는 지능형 알림 서비스입니다.

## 📋 목차

- [주요 기능](#주요-기능)
- [시스템 아키텍처](#시스템-아키텍처)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [사용 방법](#사용-방법)
- [API 문서](#api-문서)
- [샘플 규칙](#샘플-규칙)
- [아키텍처 문서](#아키텍처-문서)

## ✨ 주요 기능

### 1. 자연어 기반 알림 규칙 생성
사용자가 일상 언어로 알림 조건을 입력하면, Claude AI가 자동으로 Java 코드를 생성합니다.

```
입력: "날씨가 영하가 되면 알림해줘"
→ Claude AI가 NotificationRule 인터페이스를 구현하는 코드 자동 생성
```

### 2. 동적 코드 컴파일 및 실행
Java Compiler API를 사용하여 런타임에 코드를 컴파일하고 즉시 실행합니다.

### 3. 다양한 데이터 소스 지원
- **날씨**: 온도, 날씨 상태, 습도 등
- **주식**: 주가, 변동률, 거래량 등
- **뉴스**: 헤드라인, 키워드 검색 등

### 4. 실시간 알림
WebSocket을 통한 실시간 푸시 알림 지원

### 5. 유연한 스케줄링
Cron 표현식으로 규칙 실행 주기를 자유롭게 설정

## 🏗️ 시스템 아키텍처

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│   Client    │─────>│   API Gateway    │─────>│   Claude    │
│  (Web/App)  │<─────│  (REST/WebSocket)│      │   API       │
└─────────────┘      └──────────────────┘      └─────────────┘
                              │
                              v
                     ┌─────────────────┐
                     │ Rule Management │
                     │    Service      │
                     └─────────────────┘
                              │
            ┌─────────────────┼─────────────────┐
            v                 v                 v
   ┌────────────┐    ┌────────────┐    ┌────────────┐
   │  Dynamic   │    │ Scheduler  │    │Notification│
   │   Code     │    │  (Quartz)  │    │  Engine    │
   │  Engine    │    └────────────┘    └────────────┘
   └────────────┘            │                 │
            │                v                 v
            │       ┌─────────────┐    ┌─────────────┐
            │       │  External   │    │  WebSocket  │
            │       │  API Client │    │   Server    │
            │       └─────────────┘    └─────────────┘
            v
    ┌──────────────┐
    │  PostgreSQL  │
    │   Database   │
    └──────────────┘
```

## 🛠️ 기술 스택

### Backend
- **Java 17**: 최신 LTS 버전
- **Spring Boot 3.2**: 웹 프레임워크
- **Spring Data JPA**: ORM
- **Spring WebSocket**: 실시간 통신
- **Quartz Scheduler**: 작업 스케줄링

### AI & 코드 실행
- **Claude API (Anthropic)**: 자연어 → 코드 변환
- **Java Compiler API**: 런타임 코드 컴파일
- **Custom ClassLoader**: 동적 클래스 로딩

### Database
- **PostgreSQL**: 메인 데이터베이스
- **Flyway**: 데이터베이스 마이그레이션

### 외부 API (예정)
- **OpenWeatherMap API**: 날씨 정보
- **Yahoo Finance API**: 주식 정보
- **News API**: 뉴스 정보

## 🚀 시작하기

### 필수 요구사항

- JDK 17 이상
- PostgreSQL 14 이상
- Gradle 8.x
- Claude API Key (Anthropic)

### 환경 설정

1. **레포지토리 클론**
```bash
git clone https://github.com/basilro/ai-notification-service.git
cd ai-notification-service
```

2. **PostgreSQL 데이터베이스 생성**
```sql
CREATE DATABASE notification_db;
```

3. **환경 변수 설정**
```bash
export CLAUDE_API_KEY="your-claude-api-key"
export WEATHER_API_KEY="your-weather-api-key"
export STOCK_API_KEY="your-stock-api-key"
```

또는 `src/main/resources/application.yml` 파일을 수정하세요.

4. **빌드 및 실행**
```bash
./gradlew clean build
./gradlew bootRun
```

애플리케이션이 `http://localhost:8080`에서 실행됩니다.

## 📖 사용 방법

### 1. 알림 규칙 생성

**요청:**
```bash
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "request": "날씨가 영하가 되면 알림해줘",
    "cronExpression": "0 0/10 * * * ?"
  }'
```

**응답:**
```json
{
  "id": 1,
  "userId": "user123",
  "naturalLanguageRequest": "날씨가 영하가 되면 알림해줘",
  "generatedCode": "package com.example.notification.rules;\n\nimport...",
  "className": "TemperatureBelowZeroRule",
  "active": true,
  "cronExpression": "0 0/10 * * * ?",
  "createdAt": "2024-01-15T10:30:00",
  "executionCount": 0,
  "notificationCount": 0
}
```

### 2. 규칙 목록 조회

```bash
curl -X GET "http://localhost:8080/api/rules?userId=user123"
```

### 3. 특정 규칙 조회

```bash
curl -X GET http://localhost:8080/api/rules/1
```

### 4. 규칙 비활성화

```bash
curl -X PATCH http://localhost:8080/api/rules/1/deactivate
```

### 5. 규칙 삭제

```bash
curl -X DELETE http://localhost:8080/api/rules/1
```

### 6. WebSocket 실시간 알림 수신

**JavaScript 예제:**
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // 사용자별 알림 구독
    stompClient.subscribe('/user/queue/notifications', function(message) {
        console.log('알림 수신:', message.body);
        // 예: "⚠️ 한파 주의! 현재 온도가 -3.2℃로 영하입니다. 날씨: Clear"
    });
});
```

## 📚 API 문서

### REST API 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/rules` | 새로운 알림 규칙 생성 |
| GET | `/api/rules?userId={userId}` | 사용자의 모든 규칙 조회 |
| GET | `/api/rules/{ruleId}` | 특정 규칙 상세 조회 |
| PATCH | `/api/rules/{ruleId}/deactivate` | 규칙 비활성화 |
| DELETE | `/api/rules/{ruleId}` | 규칙 삭제 |

### WebSocket 엔드포인트

| Endpoint | Description |
|----------|-------------|
| `/ws` | WebSocket 연결 엔드포인트 (SockJS) |
| `/user/queue/notifications` | 사용자별 알림 구독 채널 |

### 데이터 모델

#### CreateRuleRequest
```json
{
  "userId": "string (필수)",
  "request": "string (필수) - 자연어 알림 요청",
  "cronExpression": "string (선택) - 기본값: 0 0/10 * * * ?"
}
```

#### RuleResponse
```json
{
  "id": "number",
  "userId": "string",
  "naturalLanguageRequest": "string",
  "generatedCode": "string",
  "className": "string",
  "active": "boolean",
  "cronExpression": "string",
  "createdAt": "datetime",
  "lastExecutedAt": "datetime",
  "executionCount": "number",
  "notificationCount": "number"
}
```

## 🎯 샘플 규칙

### 1. 온도 기반 알림
```
요청: "날씨가 영하가 되면 알림해줘"
```
- 10분마다 날씨 API를 호출하여 온도 확인
- 온도가 0도 미만일 때 알림 발송

**생성된 코드:**
```java
public class TemperatureBelowZeroRule implements NotificationRule {
    @Override
    public boolean shouldNotify(NotificationContext context) {
        double temperature = (double) context.getWeatherData().get("temperature");
        return temperature < 0.0;
    }
    
    @Override
    public String getMessage(NotificationContext context) {
        double temperature = (double) context.getWeatherData().get("temperature");
        return String.format("⚠️ 한파 주의! 현재 온도가 %.1f℃로 영하입니다.", temperature);
    }
}
```

### 2. 주가 기반 알림
```
요청: "코스피가 3000을 넘으면 알림해줘"
```
- 10분마다 주식 API를 호출하여 코스피 지수 확인
- 코스피 지수가 3000을 초과할 때 알림 발송

**생성된 코드:**
```java
public class StockPriceAboveThresholdRule implements NotificationRule {
    private static final double THRESHOLD = 3000.0;
    
    @Override
    public boolean shouldNotify(NotificationContext context) {
        double price = (double) context.getStockData().get("price");
        return price > THRESHOLD;
    }
    
    @Override
    public String getMessage(NotificationContext context) {
        double price = (double) context.getStockData().get("price");
        return String.format("📈 코스피 지수가 %.2f로 3000을 돌파했습니다!", price);
    }
}
```

## 📄 아키텍처 문서

전체 시스템 아키텍처 및 기술 설계에 대한 상세한 내용은 [ARCHITECTURE.md](docs/ARCHITECTURE.md) 문서를 참고하세요.

주요 내용:
- 시스템 구성 요소 상세 설명
- 데이터베이스 스키마 설계
- 보안 아키텍처 (Sandbox, SecurityManager)
- 기술 스택 선정 이유
- 확장성 및 성능 고려사항
- 구현 로드맵

## 🔐 보안

### 코드 실행 보안

동적으로 생성된 코드는 다음과 같은 보안 제약이 적용됩니다:

1. **제한된 패키지 접근**
   - `java.lang`, `java.util`, `java.time`, `java.math` 패키지만 허용
   - 파일 I/O, 네트워크 접속 금지

2. **실행 시간 제한**
   - 최대 실행 시간: 5초
   - 무한 루프 방지

3. **메모리 제한**
   - 최대 메모리: 50MB
   - OutOfMemory 공격 방지

4. **SecurityManager** (프로덕션 권장)
   - 시스템 자원 접근 차단
   - 민감한 API 호출 차단

## 🧪 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### 통합 테스트 실행
```bash
./gradlew integrationTest
```

## 🚧 향후 계획

- [ ] MongoDB 통합 (알림 히스토리)
- [ ] Redis 캐싱
- [ ] Email/SMS 알림 채널 추가
- [ ] 관리자 대시보드 (React)
- [ ] 멀티 테넌시 지원
- [ ] Docker 컨테이너화
- [ ] Kubernetes 배포 설정
- [ ] 모니터링 (Prometheus + Grafana)

## 📝 라이선스

MIT License

## 👨‍💻 개발자

**장범철 (Jang Beomcheol)**
- GitHub: [@basilro](https://github.com/basilro)
- 8년차 Full Stack Developer

## 🤝 기여하기

Pull Request와 Issue는 언제나 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 Issue를 등록해주세요.
