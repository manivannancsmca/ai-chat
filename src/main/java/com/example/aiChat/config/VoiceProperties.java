package com.example.aiChat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "app.voice")
public record VoiceProperties(
        String provider,
        List<String> supportedAudioTypes,
        long maxAudioSizeBytes,
        Stt stt,
        Tts tts
) {
    public record Stt(String language) {}
    public record Tts(String voice, int sampleRate) {}
}