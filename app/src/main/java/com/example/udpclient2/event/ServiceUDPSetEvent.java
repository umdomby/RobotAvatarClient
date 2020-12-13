package com.example.udpclient2.event;

public class ServiceUDPSetEvent extends DataEvent<byte[]> {
    public ServiceUDPSetEvent(byte[] text) {
        super(text);
    }
}