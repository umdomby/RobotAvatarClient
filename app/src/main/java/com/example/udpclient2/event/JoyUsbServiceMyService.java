package com.example.udpclient2.event;

public class JoyUsbServiceMyService extends DataEvent<byte[]> {
    public JoyUsbServiceMyService(byte[] text) {
        super(text);
    }
}
