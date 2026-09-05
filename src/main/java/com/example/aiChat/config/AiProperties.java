package com.example.aiChat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String systemPrompt,
        int maxMessageLength,
        int maxHistoryMessages
) {}