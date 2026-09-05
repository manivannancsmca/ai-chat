package com.example.aiChat.repository;

import com.example.aiChat.model.Conversation;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryConversationRepository implements ConversationRepository {

    private final Map<String, Conversation> store = new ConcurrentHashMap<>();

    @Override
    public Conversation save(Conversation conversation) {
        if (conversation == null) {
            throw new IllegalArgumentException("conversation must not be null");
        }
        store.put(conversation.getId(), conversation);
        return conversation;
    }

    @Override
    public Optional<Conversation> findById(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(conversationId));
    }

    @Override
    public void deleteById(String conversationId) {
        if (conversationId != null) {
            store.remove(conversationId);
        }
    }
}