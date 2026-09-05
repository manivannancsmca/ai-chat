package com.example.aiChat.voice;

public interface TextToSpeechProvider {
    byte[] synthesize(String text);
}
