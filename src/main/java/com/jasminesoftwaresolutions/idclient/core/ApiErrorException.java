package com.jasminesoftwaresolutions.idclient.core;

/**
 * Thrown when the server returns an error HTTP status with a structured API error body.
 */
public class ApiErrorException extends IDClientException {
    private final int statusCode;
    private final String error;
    private final String errorDescription;

    public ApiErrorException(int statusCode, String error, String errorDescription) {
        super("API error " + statusCode + ": " + error
                + (errorDescription != null ? " — " + errorDescription : ""));
        this.statusCode = statusCode;
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
