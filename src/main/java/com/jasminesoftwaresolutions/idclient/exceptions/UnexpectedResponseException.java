package com.jasminesoftwaresolutions.idclient.exceptions;

public class UnexpectedResponseException extends RuntimeException {
    public UnexpectedResponseException(String response, String expected, Throwable cause) {
        super("Response received, but not expected (" + expected + "): \n" + response, cause);
    }
}
