package com.example.aiChat.service;

import com.example.aiChat.config.AiProperties;
import com.example.aiChat.exception.AiServiceException;
import com.example.aiChat.model.Conversation;
import com.example.aiChat.model.ConversationMessage;
import com.example.aiChat.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final AiProperties aiProperties;

    public ConversationService(ConversationRepository conversationRepository,
                               AiProperties aiProperties) {
        this.conversationRepository = conversationRepository;
        this.aiProperties = aiProperties;
    }

    public Conversation getOrCreate(String conversationId) {
        if (conversationId != null && !conversationId.isBlank()) {
            return conversationRepository.findById(conversationId)
                    .orElseGet(() -> conversationRepository.save(Conversation.create(conversationId)));
        }
        return conversationRepository.save(Conversation.create());
    }

    public Conversation require(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AiServiceException(
                        "CONVERSATION_NOT_FOUND",
                        "Conversation not found: " + conversationId));
    }

    public void appendUserAndAssistant(Conversation conversation, String userText, String assistantText) {
        conversation.addUserMessage(userText);
        conversation.addAssistantMessage(assistantText);
        trimIfNeeded(conversation);
        conversationRepository.save(conversation);
    }

    public void appendUser(Conversation conversation, String userText) {
        conversation.addUserMessage(userText);
        trimIfNeeded(conversation);
        conversationRepository.save(conversation);
    }

    public void appendAssistant(Conversation conversation, String assistantText) {
        conversation.addAssistantMessage(assistantText);
        trimIfNeeded(conversation);
        conversationRepository.save(conversation);
    }

    public List<ConversationMessage> historyForPrompt(Conversation conversation) {
        return conversation.getRecentMessages(aiProperties.maxHistoryMessages());
    }

    private void trimIfNeeded(Conversation conversation) {
        int max = aiProperties.maxHistoryMessages();
        // Simple trim: keep only the most recent messages by rebuilding if oversized.
        // For a richer policy you could drop oldest non-system messages.
        if (conversation.size() > max * 2) {
            List<ConversationMessage> recent = conversation.getRecentMessages(max);
            Conversation trimmed = Conversation.create(conversation.getId());
            recent.forEach(trimmed::addMessage);
            conversationRepository.save(trimmed);
        }
    }
}