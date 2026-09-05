package com.example.aiChat.service;

import com.example.aiChat.ai.AiChatService;
import com.example.aiChat.config.AiProperties;
import com.example.aiChat.dto.ChatRequest;
import com.example.aiChat.dto.ChatResponse;
import com.example.aiChat.model.Conversation;
import com.example.aiChat.model.ConversationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final AiChatService aiChatService;
    private final ConversationService conversationService;
    private final AiProperties aiProperties;

    public ChatService(AiChatService aiChatService,
                       ConversationService conversationService,
                       AiProperties aiProperties) {
        this.aiChatService = aiChatService;
        this.conversationService = conversationService;
        this.aiProperties = aiProperties;
    }

    public ChatResponse chat(ChatRequest request) {
        validateMessageLength(request.message());

        Conversation conversation = conversationService.getOrCreate(request.conversationId());
        List<ConversationMessage> history = conversationService.historyForPrompt(conversation);

        log.info("Chat request conversationId={}", conversation.getId());

        String answer = aiChatService.chat(history, request.message());

        conversationService.appendUserAndAssistant(conversation, request.message(), answer);

        return new ChatResponse(
                conversation.getId(),
                answer,
                aiChatService.getModelName()
        );
    }

    /**
     * Streams tokens. History is loaded once; the full assistant reply is
     * accumulated and persisted when the stream completes.
     */
    public Flux<String> stream(ChatRequest request) {
        validateMessageLength(request.message());

        Conversation conversation = conversationService.getOrCreate(request.conversationId());
        List<ConversationMessage> history = conversationService.historyForPrompt(conversation);

        // Persist user turn up front so reconnects see the question
        conversationService.appendUser(conversation, request.message());

        StringBuilder fullResponse = new StringBuilder();

        return aiChatService.stream(history, request.message())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    String answer = fullResponse.toString();
                    conversationService.appendAssistant(conversation, answer);
                    log.info("Stream completed conversationId={} length={}",
                            conversation.getId(), answer.length());
                })
                .doOnError(e -> log.error("Stream failed conversationId={}",
                        conversation.getId(), e));
    }

    private void validateMessageLength(String message) {
        if (message != null && message.length() > aiProperties.maxMessageLength()) {
            throw new IllegalArgumentException(
                    "Message exceeds maximum length of " + aiProperties.maxMessageLength());
        }
    }
    
}
