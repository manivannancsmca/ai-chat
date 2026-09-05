package com.example.aiChat.voice;

import java.io.InputStream;

public interface SpeechToTextProvider {
    String transcribe(InputStream audioStream, String contentType);
}
