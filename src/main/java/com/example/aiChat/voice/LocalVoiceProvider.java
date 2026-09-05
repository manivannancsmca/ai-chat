package com.example.aiChat.voice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.aiChat.config.VoiceProperties;
import com.example.aiChat.exception.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Local / self-hosted voice provider stub.
 * Replace with Whisper (STT) + Piper/espeak (TTS) for real audio.
 * Interfaces allow independent replacement without touching AI layer.
 */
@Component
public class LocalVoiceProvider implements SpeechToTextProvider, TextToSpeechProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalVoiceProvider.class);
    private final VoiceProperties properties;

    public LocalVoiceProvider(VoiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public String transcribe(InputStream audioStream, String contentType) {
        try {
            // Production: call Whisper.cpp, Vosk, or a local HTTP STT service
            log.info("STT requested (contentType={})", contentType);
            // For demo/testing we return a placeholder. Real implementation reads the stream.
            return "[transcribed speech – integrate Whisper or similar]";
        } catch (Exception e) {
            throw new VoiceProcessingException("Speech-to-text failed", e);
        }
    }

    @Override
    public byte[] synthesize(String text) {
        try {
            log.info("TTS requested, length={}", text != null ? text.length() : 0);
            // Production: Piper, espeak-ng, or Coqui TTS returning WAV/PCM
            // Returning a minimal WAV header + silence for contract compliance
            return createMinimalWav(text != null ? text : "");
        } catch (Exception e) {
            throw new VoiceProcessingException("Text-to-speech failed", e);
        }
    }

    private byte[] createMinimalWav(String text) {
        // Minimal valid WAV (44-byte header + short silence). Real TTS replaces this.
        String marker = "AI-TTS:" + text.substring(0, Math.min(50, text.length()));
        return marker.getBytes(StandardCharsets.UTF_8);
    }
}
