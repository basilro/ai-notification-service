-- 알림 규칙 테이블
CREATE TABLE notification_rules (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    natural_language_request VARCHAR(1000) NOT NULL,
    generated_code TEXT NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    cron_expression VARCHAR(100) NOT NULL DEFAULT '0 0/10 * * * ?',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_executed_at TIMESTAMP,
    execution_count INTEGER NOT NULL DEFAULT 0,
    notification_count INTEGER NOT NULL DEFAULT 0
);

-- 사용자 ID 인덱스
CREATE INDEX idx_notification_rules_user_id ON notification_rules(user_id);

-- 활성 상태 인덱스
CREATE INDEX idx_notification_rules_active ON notification_rules(active);

-- 복합 인덱스
CREATE INDEX idx_notification_rules_user_active ON notification_rules(user_id, active);
