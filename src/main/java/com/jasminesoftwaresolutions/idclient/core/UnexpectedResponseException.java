package com.jasminesoftwaresolutions.idclient.core;

/**
 * Thrown when the server returns an unexpected or non-JSON response body.
 */
public class UnexpectedResponseException extends IDClientException {
    private final String rawBody;

    public UnexpectedResponseException(String rawBody, String expected, Throwable cause) {
        super("Response received, but not expected (" + expected + "): \n" + rawBody, cause);
        this.rawBody = rawBody;
    }

    public String getRawBody() {
        return rawBody;
    }
}
