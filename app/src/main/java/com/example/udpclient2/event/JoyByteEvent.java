package com.example.udpclient2.event;

public class JoyByteEvent extends DataEvent<byte[]> {
    public JoyByteEvent(byte[] text) {
        super(text);
    }
}