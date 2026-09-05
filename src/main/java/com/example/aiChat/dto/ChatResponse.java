package com.example.aiChat.dto;

public record ChatResponse(
        String conversationId,
        String message,
        String model
) {}
