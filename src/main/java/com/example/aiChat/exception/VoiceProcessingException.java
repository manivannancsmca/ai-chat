package com.example.aiChat.exception;

/**
 * Thrown when speech-to-text or text-to-speech processing fails,
 * or when the uploaded audio is invalid / unsupported.
 */
public class VoiceProcessingException extends RuntimeException {

    private final String errorCode;

    public VoiceProcessingException(String message) {
        super(message);
        this.errorCode = "VOICE_PROCESSING_ERROR";
    }

    public VoiceProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VOICE_PROCESSING_ERROR";
    }

    public VoiceProcessingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public VoiceProcessingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}