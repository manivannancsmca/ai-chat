package com.example.aiChat.ai;

import reactor.core.publisher.Flux;
import java.util.List;

import com.example.aiChat.model.ConversationMessage;

public interface AiChatService {
    String chat(List<ConversationMessage> history, String userMessage);
    Flux<String> stream(List<ConversationMessage> history, String userMessage);
    String getModelName();
}
