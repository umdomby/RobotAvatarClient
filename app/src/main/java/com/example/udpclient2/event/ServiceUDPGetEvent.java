package com.example.udpclient2.event;

public class ServiceUDPGetEvent extends DataEvent<byte[]> {
    public ServiceUDPGetEvent(byte[] text) {
        super(text);
    }
}