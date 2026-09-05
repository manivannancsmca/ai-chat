package com.example.aiChat.controller;

import com.example.aiChat.dto.VoiceChatResponse;
import com.example.aiChat.service.VoiceChatService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/voice-chat")
public class VoiceChatController {

    private final VoiceChatService voiceChatService;

    public VoiceChatController(VoiceChatService voiceChatService) {
        this.voiceChatService = voiceChatService;
    }

    /**
     * Accepts multipart audio + optional conversationId.
     * Returns JSON metadata including base64 audio for simplicity.
     * (Binary-only response can be added later if preferred.)
     */
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceChatResponse> voiceChat(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam(value = "conversationId", required = false) String conversationId) {

        VoiceChatResponse response = voiceChatService.process(audio, conversationId);
        return ResponseEntity.ok(response);
    }
    
}
