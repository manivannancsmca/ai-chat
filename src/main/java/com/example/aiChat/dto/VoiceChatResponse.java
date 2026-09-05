package com.example.aiChat.dto;

/**
 * Metadata returned alongside the audio response.
 * Controllers may return this as JSON + separate audio bytes,
 * or embed base64 audio when preferred.
 */
public record VoiceChatResponse(
        String conversationId,
        String transcribedText,
        String aiResponseText,
        String model,
        String audioContentType,
        /** Base64-encoded audio when returning JSON; null when binary is streamed separately */
        String audioBase64
) {}
