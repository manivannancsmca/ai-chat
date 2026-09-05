package com.example.aiChat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Message must not be blank")
        @Size(max = 10000, message = "Message must not exceed 10000 characters")
        String message,

        String conversationId
) {}