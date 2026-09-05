package com.example.aiChat.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * In-memory representation of a conversation.
 * Messages are stored in chronological order.
 * This class is mutable only through controlled methods so the repository
 * can update it safely.
 */
public class Conversation {

    private final String id;
    private final List<ConversationMessage> messages;
    private final Instant createdAt;
    private Instant updatedAt;

    public Conversation(String id) {
        this.id = Objects.requireNonNullElseGet(id, () -> UUID.randomUUID().toString());
        this.messages = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /** Creates a new conversation with a generated ID. */
    public static Conversation create() {
        return new Conversation(null);
    }

    /** Creates a conversation with the given ID (or generates one if null/blank). */
    public static Conversation create(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return create();
        }
        return new Conversation(conversationId);
    }

    public String getId() {
        return id;
    }

    public List<ConversationMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void addMessage(ConversationMessage message) {
        Objects.requireNonNull(message, "message must not be null");
        messages.add(message);
        this.updatedAt = Instant.now();
    }

    public void addUserMessage(String content) {
        addMessage(ConversationMessage.user(content));
    }

    public void addAssistantMessage(String content) {
        addMessage(ConversationMessage.assistant(content));
    }

    public void addSystemMessage(String content) {
        addMessage(ConversationMessage.system(content));
    }

    /**
     * Returns a defensive copy of the message history,
     * optionally limited to the last {@code maxMessages} entries.
     */
    public List<ConversationMessage> getRecentMessages(int maxMessages) {
        if (maxMessages <= 0 || messages.size() <= maxMessages) {
            return List.copyOf(messages);
        }
        return List.copyOf(messages.subList(messages.size() - maxMessages, messages.size()));
    }

    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }
}