package com.example.aiChat.service;

import com.example.aiChat.ai.AiChatService;
import com.example.aiChat.config.VoiceProperties;
import com.example.aiChat.dto.VoiceChatResponse;
import com.example.aiChat.exception.VoiceProcessingException;
import com.example.aiChat.model.Conversation;
import com.example.aiChat.model.ConversationMessage;
import com.example.aiChat.voice.SpeechToTextProvider;
import com.example.aiChat.voice.TextToSpeechProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VoiceChatService {

    private static final Logger log = LoggerFactory.getLogger(VoiceChatService.class);

    private final SpeechToTextProvider speechToTextProvider;
    private final TextToSpeechProvider textToSpeechProvider;
    private final AiChatService aiChatService;
    private final ConversationService conversationService;
    private final VoiceProperties voiceProperties;

    public VoiceChatService(SpeechToTextProvider speechToTextProvider,
            TextToSpeechProvider textToSpeechProvider,
            AiChatService aiChatService,
            ConversationService conversationService,
            VoiceProperties voiceProperties) {
        this.speechToTextProvider = speechToTextProvider;
        this.textToSpeechProvider = textToSpeechProvider;
        this.aiChatService = aiChatService;
        this.conversationService = conversationService;
        this.voiceProperties = voiceProperties;
    }

    public VoiceChatResponse process(MultipartFile audio, String conversationId) {
        validateAudio(audio);

        String contentType = audio.getContentType() != null
                ? audio.getContentType()
                : "application/octet-stream";

        // 1. Speech → Text
        String transcribed;
        try (InputStream in = audio.getInputStream()) {
            long start = System.currentTimeMillis();
            transcribed = speechToTextProvider.transcribe(in, contentType);
            log.info("STT completed in {} ms", System.currentTimeMillis() - start);
        } catch (VoiceProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new VoiceProcessingException("Failed to read or transcribe audio", e);
        }

        if (transcribed == null || transcribed.isBlank()) {
            throw new VoiceProcessingException(
                    "EMPTY_TRANSCRIPTION",
                    "Could not transcribe any speech from the audio");
        }

        // 2. Text → AI
        Conversation conversation = conversationService.getOrCreate(conversationId);
        List<ConversationMessage> history = conversationService.historyForPrompt(conversation);

        long aiStart = System.currentTimeMillis();
        String aiText = aiChatService.chat(history, transcribed);
        log.info("AI response for voice chat in {} ms", System.currentTimeMillis() - aiStart);

        conversationService.appendUserAndAssistant(conversation, transcribed, aiText);

        // 3. Text → Speech
        byte[] audioBytes;
        try {
            long ttsStart = System.currentTimeMillis();
            audioBytes = textToSpeechProvider.synthesize(aiText);
            log.info("TTS completed in {} ms, bytes={}",
                    System.currentTimeMillis() - ttsStart,
                    audioBytes != null ? audioBytes.length : 0);
        } catch (VoiceProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new VoiceProcessingException("Text-to-speech failed", e);
        }

        String audioBase64 = audioBytes != null
                ? Base64.getEncoder().encodeToString(audioBytes)
                : null;

        return new VoiceChatResponse(
                conversation.getId(),
                transcribed,
                aiText,
                aiChatService.getModelName(),
                "audio/wav", // adjust when real TTS format is known
                audioBase64);
    }

    private void validateAudio(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new VoiceProcessingException(
                    "INVALID_AUDIO",
                    "Audio file is required and must not be empty");
        }

        if (audio.getSize() > voiceProperties.maxAudioSizeBytes()) {
            throw new VoiceProcessingException(
                    "AUDIO_TOO_LARGE",
                    "Audio file exceeds maximum size of " + voiceProperties.maxAudioSizeBytes() + " bytes");
        }

        String contentType = audio.getContentType();
        String baseType = normalizeContentType(contentType);

        Set<String> allowed = voiceProperties.supportedAudioTypes().stream()
                .map(this::normalizeContentType)
                .collect(Collectors.toSet());

        if (baseType == null || !allowed.contains(baseType)) {
            throw new VoiceProcessingException(
                    "UNSUPPORTED_AUDIO_FORMAT",
                    "Unsupported audio type: " + contentType + ". Allowed: " + allowed);
        }
    }

    /** Strips parameters (e.g. ";codecs=opus") and lowercases. */
    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        String trimmed = contentType.trim().toLowerCase();
        int semi = trimmed.indexOf(';');
        return semi >= 0 ? trimmed.substring(0, semi).trim() : trimmed;
    }
}