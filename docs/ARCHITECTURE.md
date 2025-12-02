# AI 알림 서비스 아키텍처 설계

## 📋 목차

1. [시스템 개요](#시스템-개요)
2. [전체 아키텍처](#전체-아키텍처)
3. [핵심 컴포넌트](#핵심-컴포넌트)
4. [데이터 플로우](#데이터-플로우)
5. [기술 스택](#기술-스택)
6. [보안 설계](#보안-설계)
7. [확장성 및 성능](#확장성-및-성능)
8. [구현 로드맵](#구현-로드맵)

---

## 시스템 개요

### 🎯 서비스 목적
사용자가 자연어로 알림 조건을 요청하면, AI(Claude)가 자동으로 코드를 생성하고, 
시스템이 이를 동적으로 실행하여 조건 만족 시 알림을 발송하는 지능형 알림 서비스

### 💡 핵심 가치
- **Zero-Code**: 코딩 없이 자연어로 복잡한 알림 규칙 생성
- **AI-Powered**: Claude가 최적의 코드와 API를 자동 선택
- **Dynamic**: 런타임에 규칙 추가/수정/삭제
- **Extensible**: 새로운 데이터 소스 자동 통합

### 🌟 사용 예시
```
사용자: "날씨가 영하가 되면 알림해줘"
      ↓
AI: 날씨 API 선택 + 조건 코드 생성
      ↓
시스템: 1시간마다 체크 → 영하 감지 시 알림 발송
```

---

## 전체 아키텍처

### 시스템 구조도

```
┌─────────────────────────────────────────────────────────────┐
│                         Client Layer                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ Web UI   │  │ Mobile   │  │  API     │  │ WebSocket│    │
│  │          │  │   App    │  │  Client  │  │          │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │   Spring Boot REST API + WebSocket Server           │   │
│  │   - Authentication & Authorization                   │   │
│  │   - Rate Limiting                                    │   │
│  │   - Request Validation                               │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐    │
│  │ Rule Manager  │  │ Code Generator│  │  Scheduler   │    │
│  │               │  │  (Claude AI)  │  │  (Quartz)    │    │
│  └───────────────┘  └───────────────┘  └──────────────┘    │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐    │
│  │ Code Compiler │  │ Rule Executor │  │ Notifier     │    │
│  │ (Java API)    │  │  (Sandbox)    │  │              │    │
│  └───────────────┘  └───────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Data Access Layer                        │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐    │
│  │   PostgreSQL  │  │     Redis     │  │   MongoDB    │    │
│  │   (Rules DB)  │  │   (Cache)     │  │  (Logs DB)   │    │
│  └───────────────┘  └───────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    External Services                         │
│  ┌───────────┐  ┌───────────┐  ┌──────────┐  ┌─────────┐   │
│  │ Claude AI │  │ Weather   │  │  Stock   │  │  News   │   │
│  │    API    │  │    API    │  │   API    │  │   API   │   │
│  └───────────┘  └───────────┘  └──────────┘  └─────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 핵심 컴포넌트

### 1. 🤖 AI Code Generator (Claude 통합)

**역할**: 자연어를 실행 가능한 Java 코드로 변환

```java
/**
 * Claude API를 통해 사용자 요청을 분석하고 코드를 생성합니다
 */
@Service
public class ClaudeCodeGenerator {
    
    /**
     * 자연어 요청을 분석하여 알림 규칙 코드 생성
     * 
     * @param userRequest 사용자의 자연어 요청
     * @return 생성된 규칙 코드와 메타데이터
     */
    public RuleCodeResponse generateRuleCode(String userRequest) {
        // 1. Claude에게 요청 분석 요청
        // 2. 필요한 외부 API 식별
        // 3. Java 코드 생성
        // 4. 테스트 코드 생성
        // 5. 메타데이터 추출 (체크 주기, 알림 메시지 등)
    }
}
```

**입력 예시**:
```
"날씨가 영하가 되고 미세먼지가 나쁨이면 알림해줘"
```

**출력 예시**:
```json
{
  "ruleCode": "public class WeatherRule implements NotificationRule {...}",
  "testCode": "public class WeatherRuleTest {...}",
  "metadata": {
    "checkInterval": "PT1H",
    "requiredApis": ["openweathermap", "airkorea"],
    "description": "기온 영하 & 미세먼지 나쁨 감지"
  }
}
```

### 2. ⚙️ Dynamic Code Engine

**역할**: 생성된 코드를 런타임에 컴파일하고 실행

```java
/**
 * Java Compiler API를 사용하여 동적으로 코드를 컴파일하고 로드합니다
 */
@Service
public class DynamicCodeEngine {
    
    /**
     * 소스 코드를 컴파일하고 클래스를 로드합니다
     * 
     * @param sourceCode Java 소스 코드
     * @param className 클래스 이름
     * @return 로드된 클래스
     */
    public Class<?> compileAndLoad(String sourceCode, String className) {
        // 1. JavaCompiler API로 컴파일
        // 2. CustomClassLoader로 로드
        // 3. 인터페이스 검증
        // 4. 리플렉션으로 인스턴스 생성 가능 여부 체크
    }
    
    /**
     * 샌드박스 환경에서 규칙 테스트 실행
     */
    public TestResult testRule(NotificationRule rule) {
        // Docker 컨테이너 또는 SecurityManager로 격리
    }
}
```

**컴파일 프로세스**:
```
Source Code → JavaCompiler → ByteCode → ClassLoader → Class<?> → Instance
```

### 3. 📋 Rule Manager

**역할**: 알림 규칙의 생명주기 관리

```java
/**
 * 알림 규칙의 CRUD 및 상태 관리
 */
@Service
public class RuleManager {
    
    /**
     * 새로운 규칙 등록
     */
    public RuleEntity createRule(CreateRuleRequest request) {
        // 1. Claude로 코드 생성
        // 2. 코드 컴파일 및 검증
        // 3. 샌드박스 테스트
        // 4. DB 저장
        // 5. 스케줄러 등록
    }
    
    /**
     * 규칙 활성화/비활성화
     */
    public void toggleRule(Long ruleId, boolean active) {
        // 스케줄러에서 추가/제거
    }
    
    /**
     * 규칙 삭제
     */
    public void deleteRule(Long ruleId) {
        // 1. 스케줄러에서 제거
        // 2. DB에서 삭제
        // 3. 클래스 언로드
    }
}
```

### 4. ⏰ Scheduler (Quartz)

**역할**: 각 규칙을 주기적으로 실행

```java
/**
 * Quartz Scheduler를 사용한 규칙 실행
 */
@Service
public class RuleScheduler {
    
    /**
     * 규칙을 스케줄에 등록
     */
    public void scheduleRule(RuleEntity rule) {
        JobDetail job = JobBuilder.newJob(RuleExecutionJob.class)
            .withIdentity("rule-" + rule.getId())
            .usingJobData("ruleId", rule.getId())
            .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withSchedule(SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(rule.getCheckIntervalSeconds())
                .repeatForever())
            .build();
        
        scheduler.scheduleJob(job, trigger);
    }
}

/**
 * 실제 규칙을 실행하는 Job
 */
public class RuleExecutionJob implements Job {
    
    @Override
    public void execute(JobExecutionContext context) {
        Long ruleId = context.getJobDetail()
            .getJobDataMap().getLong("ruleId");
        
        // 1. 규칙 인스턴스 로드
        // 2. shouldNotify() 호출
        // 3. true면 알림 발송
    }
}
```

### 5. 🔔 Notification Engine

**역할**: 다양한 채널로 알림 발송

```java
/**
 * 멀티 채널 알림 발송
 */
@Service
public class NotificationEngine {
    
    /**
     * 알림 발송 (채널별 분기)
     */
    public void sendNotification(NotificationRequest request) {
        switch (request.getChannel()) {
            case WEBSOCKET:
                webSocketHandler.send(request);
                break;
            case EMAIL:
                emailService.send(request);
                break;
            case PUSH:
                fcmService.send(request);
                break;
            case SMS:
                smsService.send(request);
                break;
        }
    }
}
```

**지원 채널**:
- WebSocket (실시간)
- Email
- Push Notification (FCM)
- SMS (Twilio)
- Slack Webhook
- Discord Webhook

### 6. 🛡️ Security Sandbox

**역할**: 생성된 코드를 안전하게 실행

```java
/**
 * 샌드박스 환경에서 코드 실행
 */
@Service
public class SandboxExecutor {
    
    /**
     * 제한된 권한으로 코드 실행
     */
    public <T> T executeInSandbox(Callable<T> task) {
        SecurityManager oldSM = System.getSecurityManager();
        try {
            // 1. 커스텀 SecurityManager 설정
            System.setSecurityManager(new RestrictiveSecurityManager());
            
            // 2. 타임아웃 설정으로 실행
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<T> future = executor.submit(task);
            return future.get(5, TimeUnit.SECONDS);
            
        } finally {
            System.setSecurityManager(oldSM);
        }
    }
}

/**
 * 제한적인 SecurityManager
 */
class RestrictiveSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
        // 파일 쓰기 금지
        if (perm instanceof FilePermission) {
            throw new SecurityException("File access denied");
        }
        // 네트워크 접근 제한 (허용된 API만)
        if (perm instanceof SocketPermission) {
            if (!isAllowedHost(perm.getName())) {
                throw new SecurityException("Network access denied");
            }
        }
    }
}
```

---

## 데이터 플로우

### 1️⃣ 규칙 생성 플로우

```
사용자 요청
    ↓
[API Gateway] 인증 & 검증
    ↓
[Rule Manager] 규칙 생성 요청
    ↓
[Claude API] 코드 생성
    ↓
[Code Engine] 컴파일 & 로드
    ↓
[Sandbox] 안전성 테스트
    ↓ (성공)
[PostgreSQL] 규칙 저장
    ↓
[Scheduler] 스케줄 등록
    ↓
[WebSocket] 사용자에게 성공 알림
```

### 2️⃣ 규칙 실행 플로우

```
[Quartz Scheduler] 정해진 시간마다 트리거
    ↓
[Rule Executor] 규칙 인스턴스 로드
    ↓
[Sandbox] shouldNotify() 실행
    ↓
[External API] 외부 데이터 조회 (날씨, 주식 등)
    ↓
[Rule Logic] 조건 검사
    ↓ (조건 만족)
[Notification Engine] 알림 메시지 생성
    ↓
[Multi-Channel] 알림 발송
    ├─ WebSocket → 실시간
    ├─ Email → 이메일
    ├─ Push → 모바일
    └─ SMS → 문자
    ↓
[MongoDB] 알림 히스토리 저장
```

### 3️⃣ 데이터베이스 스키마

#### PostgreSQL (규칙 정보)

```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 규칙 테이블
CREATE TABLE notification_rules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- 사용자 원본 요청
    user_request TEXT NOT NULL,
    
    -- 생성된 코드
    rule_code TEXT NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    
    -- 실행 설정
    check_interval_seconds INT DEFAULT 3600,
    is_active BOOLEAN DEFAULT true,
    
    -- 메타데이터
    required_apis JSONB,
    notification_channels JSONB,
    
    -- 통계
    execution_count INT DEFAULT 0,
    notification_count INT DEFAULT 0,
    last_executed_at TIMESTAMP,
    last_notified_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 실행 로그 (최근 100건만 유지)
CREATE TABLE rule_executions (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT REFERENCES notification_rules(id),
    success BOOLEAN,
    notified BOOLEAN,
    error_message TEXT,
    execution_time_ms INT,
    executed_at TIMESTAMP DEFAULT NOW()
);
```

#### MongoDB (알림 히스토리)

```javascript
// 알림 컬렉션
{
  _id: ObjectId,
  userId: Long,
  ruleId: Long,
  ruleName: String,
  message: String,
  channel: String, // "websocket", "email", "push", "sms"
  data: Object, // 알림에 포함된 데이터
  sentAt: ISODate,
  readAt: ISODate, // 읽음 여부
}
```

#### Redis (캐시)

```
# API 응답 캐시 (5분)
weather:{city}:{date} → JSON
stock:{symbol}:{timestamp} → JSON

# 사용자 세션
session:{userId} → User Object

# Rate Limiting
rate_limit:{userId}:{endpoint} → Count
```

---

## 기술 스택

### Backend
| 영역 | 기술 | 버전 | 용도 |
|-----|------|------|------|
| Framework | Spring Boot | 3.2.x | 백엔드 프레임워크 |
| Language | Java | 17+ | 메인 언어 |
| Security | Spring Security | 6.x | 인증/인가 |
| WebSocket | Spring WebSocket | - | 실시간 통신 |
| Scheduler | Quartz | 2.3.x | 작업 스케줄링 |
| Compiler | Java Compiler API | - | 동적 컴파일 |

### Database
| 유형 | 기술 | 용도 |
|-----|------|------|
| RDBMS | PostgreSQL 15 | 규칙 정보 저장 |
| Cache | Redis 7 | 캐싱, 세션 |
| NoSQL | MongoDB 6 | 알림 히스토리 |

### External Services
| 서비스 | 용도 | 비용 |
|--------|------|------|
| Claude API | 코드 생성 | 사용량 기반 |
| OpenWeatherMap | 날씨 정보 | 무료 티어 있음 |
| Alpha Vantage | 주식 정보 | 무료 티어 있음 |
| NewsAPI | 뉴스 정보 | 무료 티어 있음 |
| FCM | Push 알림 | 무료 |
| SendGrid | 이메일 | 무료 티어 있음 |

### DevOps
| 도구 | 용도 |
|-----|------|
| Docker | 컨테이너화 |
| GitHub Actions | CI/CD |
| Prometheus | 모니터링 |
| Grafana | 대시보드 |
| ELK Stack | 로그 수집 |

---

## 보안 설계

### 1. 코드 실행 보안

#### 🔒 SecurityManager를 통한 제한
```java
// 금지 항목
- System.exit() 호출
- 파일 시스템 쓰기
- 리플렉션으로 private 접근
- 클래스 로더 조작
- 네이티브 코드 실행

// 허용 항목 (화이트리스트)
- 승인된 외부 API 호출만
- 읽기 전용 파일 접근
- 표준 Java 라이브러리
```

#### ⏱️ 리소스 제한
```java
// 실행 제한
- CPU: 최대 5초 실행 시간
- Memory: 최대 100MB 힙 메모리
- Network: 화이트리스트 도메인만
- Thread: 단일 스레드만 허용
```

#### 🐳 Docker 샌드박스 (선택적)
```yaml
# 더 강력한 격리가 필요한 경우
docker run --rm \
  --memory=100m \
  --cpus=0.5 \
  --network=restricted \
  --read-only \
  rule-executor:latest
```

### 2. API 보안

#### 인증/인가
```
- JWT 토큰 기반 인증
- Role-based Access Control (USER, ADMIN)
- API Key 관리 (외부 서비스용)
```

#### Rate Limiting
```java
// 사용자별 제한
@RateLimiter(
    value = "10 requests / minute",
    keyResolver = "userKeyResolver"
)

// IP별 제한
@RateLimiter(
    value = "100 requests / hour",
    keyResolver = "ipKeyResolver"
)
```

### 3. 데이터 보안

- **암호화**: AES-256으로 민감 데이터 암호화
- **API Key 관리**: Vault에 저장, 런타임에만 로드
- **SQL Injection 방지**: PreparedStatement 사용
- **XSS 방지**: 입력 검증 및 이스케이핑

### 4. 모니터링 & 감사

```java
// 모든 규칙 실행 로깅
@Slf4j
public class AuditLogger {
    public void logExecution(RuleExecutionEvent event) {
        log.info("Rule executed: userId={}, ruleId={}, " +
                 "success={}, duration={}ms",
                 event.getUserId(),
                 event.getRuleId(),
                 event.isSuccess(),
                 event.getDurationMs());
    }
}
```

---

## 확장성 및 성능

### 수평 확장 (Scale Out)

```
┌─────────────┐
│ Load        │
│ Balancer    │
└──────┬──────┘
       │
   ┌───┴────┐
   │        │
┌──▼──┐  ┌──▼──┐
│ App │  │ App │  ← 여러 인스턴스
│  #1 │  │  #2 │
└──┬──┘  └──┬──┘
   │        │
   └───┬────┘
       ▼
  ┌────────┐
  │ Redis  │ ← 공유 세션/캐시
  │ Cluster│
  └────────┘
```

### 성능 최적화

#### 1. 캐싱 전략
```java
// L1: 애플리케이션 캐시 (Caffeine)
@Cacheable(value = "rules", key = "#ruleId")
public RuleEntity getRule(Long ruleId) { ... }

// L2: Redis 캐시
@Cacheable(value = "apiResponses", 
           key = "#api + ':' + #params",
           cacheManager = "redisCacheManager")
public ApiResponse callExternalApi(String api, Map params) { ... }
```

#### 2. 비동기 처리
```java
// 알림 발송은 비동기로
@Async
public CompletableFuture<Void> sendNotification(NotificationRequest req) {
    notificationEngine.send(req);
    return CompletableFuture.completedFuture(null);
}
```

#### 3. 데이터베이스 최적화
```sql
-- 인덱스
CREATE INDEX idx_rules_user_active 
    ON notification_rules(user_id, is_active);
CREATE INDEX idx_rules_next_exec 
    ON notification_rules(next_execution_time) 
    WHERE is_active = true;

-- 파티셔닝 (알림 히스토리)
CREATE TABLE notifications (
    ...
) PARTITION BY RANGE (sent_at);
```

### 모니터링 메트릭

```java
// Micrometer + Prometheus
@Timed("rule.execution")
@Counted("rule.execution.count")
public void executeRule(RuleEntity rule) {
    ...
}

// 주요 메트릭
- rule.execution.time: 규칙 실행 시간
- rule.execution.count: 실행 횟수
- rule.notification.count: 알림 발송 횟수
- api.call.duration: 외부 API 호출 시간
- jvm.memory.used: JVM 메모리 사용량
```

---

## 구현 로드맵

### Phase 1: MVP (2주) ✅
**목표**: 핵심 기능만 구현하여 PoC 완성

- [x] Spring Boot 프로젝트 셋업
- [x] Claude API 연동
- [x] 간단한 코드 생성 (날씨 알림)
- [x] 동적 컴파일 & 실행
- [x] 기본 스케줄러
- [x] WebSocket 알림
- [x] PostgreSQL 연동

**산출물**: 
- 날씨 알림 1개 데모
- REST API 3개 (생성/조회/삭제)
- 실시간 알림 확인

### Phase 2: 핵심 기능 (4주)
**목표**: 프로덕션 레벨 품질 확보

**Week 1-2: 보안 & 안정성**
- [ ] SecurityManager 구현
- [ ] 샌드박스 테스트
- [ ] 에러 핸들링 강화
- [ ] Rate Limiting
- [ ] 로깅 시스템

**Week 3-4: 확장성**
- [ ] Redis 캐싱
- [ ] MongoDB 연동
- [ ] 다중 알림 채널 (Email, Push)
- [ ] 규칙 템플릿 기능
- [ ] 사용자 대시보드

### Phase 3: 고급 기능 (4주)
**목표**: 차별화 기능 추가

**Week 1-2: AI 강화**
- [ ] Claude에 Few-shot 학습
- [ ] 규칙 최적화 제안
- [ ] 자동 디버깅
- [ ] 규칙 설명 생성

**Week 3-4: 커뮤니티**
- [ ] 규칙 공유 마켓플레이스
- [ ] 인기 규칙 랭킹
- [ ] 사용자 리뷰
- [ ] 규칙 포크 기능

### Phase 4: 엔터프라이즈 (6주)
**목표**: B2B 시장 진출

- [ ] 팀 워크스페이스
- [ ] 규칙 권한 관리
- [ ] SLA 모니터링
- [ ] 커스텀 알림 채널 (Slack, MS Teams)
- [ ] 감사 로그
- [ ] 백업 & 복구

---

## 예상 비용 (월 1,000명 사용자)

| 항목 | 예상 비용 (월) |
|-----|--------------|
| AWS EC2 (t3.medium × 2) | $60 |
| AWS RDS (PostgreSQL) | $50 |
| AWS ElastiCache (Redis) | $30 |
| MongoDB Atlas (Shared) | $25 |
| Claude API (10,000 req) | $200 |
| 외부 API (날씨, 주식 등) | $100 |
| SendGrid (Email) | $20 |
| FCM (Push) | $0 |
| **합계** | **$485** |

**수익 모델**:
- Free: 규칙 3개, 1시간 체크
- Pro ($9.99/월): 규칙 50개, 5분 체크
- Team ($49.99/월): 규칙 무제한, 1분 체크

---

## 기술적 도전과제

### 1. 동적 코드 실행의 안정성
**문제**: 사용자가 생성한 코드가 시스템을 다운시킬 수 있음  
**해결**: SecurityManager + 타임아웃 + 리소스 제한

### 2. Claude API 비용 최적화
**문제**: 코드 생성마다 비용 발생  
**해결**: 
- 규칙 템플릿 캐싱
- 유사 규칙 재사용
- Few-shot 학습으로 토큰 수 감소

### 3. 외부 API 장애 대응
**문제**: 날씨 API 다운 시 모든 날씨 규칙 실패  
**해결**:
- 대체 API 자동 전환
- 캐시된 데이터 사용
- 일시적 비활성화 후 자동 복구

### 4. 규칙 폭발적 증가
**문제**: 사용자 증가 시 규칙 수 폭증  
**해결**:
- 규칙 샤딩 (userId % 10)
- 스케줄러 분산
- 배치 처리

---

## 결론

이 아키텍처는 **실제 구현 가능하며 확장 가능한 설계**입니다.

### ✅ 강점
1. **혁신성**: AI 기반 자동 코드 생성
2. **유연성**: 어떤 조건이든 자연어로 표현 가능
3. **확장성**: 수평 확장 가능한 아키텍처
4. **보안**: 샌드박스 격리로 안전성 보장

### ⚠️ 리스크
1. Claude API 의존도 (대안: 로컬 모델)
2. 동적 코드 보안 (대안: 더 강력한 샌드박스)
3. 비용 증가 (대안: 효율적인 캐싱)

### 🚀 Next Steps
1. MVP 구현 (2주)
2. 베타 테스트 (100명)
3. 피드백 반영
4. 정식 출시

---

**문서 버전**: 1.0  
**최종 수정**: 2025-12-02  
**작성자**: AI Notification Service Team
