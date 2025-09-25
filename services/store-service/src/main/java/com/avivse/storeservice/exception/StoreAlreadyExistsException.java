package com.avivse.storeservice.exception;

public class StoreAlreadyExistsException extends RuntimeException {

    public StoreAlreadyExistsException(String message) {
        super(message);
    }

    public StoreAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}