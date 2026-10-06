package com.jasminesoftwaresolutions.idclient.core;

/**
 * Base exception for all ID client errors.
 */
public class IDClientException extends RuntimeException {
    public IDClientException(String message) {
        super(message);
    }

    public IDClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
