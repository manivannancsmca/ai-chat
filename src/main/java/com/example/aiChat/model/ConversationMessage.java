package com.example.aiChat.model;

import java.time.Instant;

public record ConversationMessage(
        MessageRole role,
        String content,
        Instant timestamp
) {
    public static ConversationMessage user(String content) {
        return new ConversationMessage(MessageRole.USER, content, Instant.now());
    }
    public static ConversationMessage assistant(String content) {
        return new ConversationMessage(MessageRole.ASSISTANT, content, Instant.now());
    }
    public static ConversationMessage system(String content) {
        return new ConversationMessage(MessageRole.SYSTEM, content, Instant.now());
    }
}
