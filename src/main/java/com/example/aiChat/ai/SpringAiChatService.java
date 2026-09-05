package com.example.aiChat.ai;


import com.example.aiChat.config.AiProperties;
import com.example.aiChat.exception.AiServiceException;
import com.example.aiChat.model.ConversationMessage;
import com.example.aiChat.model.MessageRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpringAiChatService implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiChatService.class);

    private final ChatClient chatClient;
    private final AiProperties aiProperties;
    private final String modelName;

    public SpringAiChatService(OllamaChatModel ollamaChatModel,
                               AiProperties aiProperties,
                               @Value("${spring.ai.ollama.chat.options.model:llama3.2}") String modelName) {
        this.chatClient = ChatClient.builder(ollamaChatModel).build();
        this.aiProperties = aiProperties;
        this.modelName = modelName;
    }

    @Override
    public String chat(List<ConversationMessage> history, String userMessage) {
        try {
            long start = System.currentTimeMillis();
            String response = chatClient.prompt()
                    .messages(toSpringAiMessages(history, userMessage))
                    .call()
                    .content();
            log.info("AI chat completed in {} ms", System.currentTimeMillis() - start);
            return response != null ? response : "";
        } catch (Exception e) {
            log.error("AI chat failed", e);
            throw new AiServiceException("Unable to communicate with the AI service", e);
        }
    }

    @Override
    public Flux<String> stream(List<ConversationMessage> history, String userMessage) {
        try {
            return chatClient.prompt()
                    .messages(toSpringAiMessages(history, userMessage))
                    .stream()
                    .content()
                    .doOnError(e -> log.error("AI streaming failed", e))
                    .onErrorMap(e -> new AiServiceException("Streaming failed", e));
        } catch (Exception e) {
            throw new AiServiceException("Unable to start AI stream", e);
        }
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    private List<Message> toSpringAiMessages(List<ConversationMessage> history, String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(aiProperties.systemPrompt()));
        if (history != null) {
            for (ConversationMessage m : history) {
                messages.add(switch (m.role()) {
                    case SYSTEM -> new SystemMessage(m.content());
                    case USER -> new UserMessage(m.content());
                    case ASSISTANT -> new AssistantMessage(m.content());
                });
            }
        }
        messages.add(new UserMessage(userMessage));
        return messages;
    }
}