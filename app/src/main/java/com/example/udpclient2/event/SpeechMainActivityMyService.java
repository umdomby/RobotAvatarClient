package com.example.udpclient2.event;

public class SpeechMainActivityMyService extends DataEvent<String> {
    public SpeechMainActivityMyService(String text) {
        super(text);
    }
}
