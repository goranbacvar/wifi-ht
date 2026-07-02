package com.wifiadmin.exception;

public class PlatformCommunicationException extends RuntimeException {

    public PlatformCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlatformCommunicationException(String message) {
        super(message);
    }
}
