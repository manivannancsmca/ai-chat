package com.example.aiChat.repository;

import java.util.Optional;

import com.example.aiChat.model.Conversation;

public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(String conversationId);
    void deleteById(String conversationId);
}
