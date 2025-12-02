package com.example.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Claude API 설정
 */
@Configuration
@ConfigurationProperties(prefix = "claude.api")
@Data
public class ClaudeConfig {
    private String key;
    private String url;
    private String model;
    private Integer maxTokens;
}
