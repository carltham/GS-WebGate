package com.noprobit.webgate.relay;

public class PortUnavailableException extends RuntimeException {
    public PortUnavailableException(String message) {
        super(message);
    }

    public PortUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
