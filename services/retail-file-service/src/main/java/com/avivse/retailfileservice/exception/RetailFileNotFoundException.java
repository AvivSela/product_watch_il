package com.avivse.retailfileservice.exception;

public class RetailFileNotFoundException extends RuntimeException {

    public RetailFileNotFoundException(String message) {
        super(message);
    }

    public RetailFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RetailFileNotFoundException forId(String id) {
        return new RetailFileNotFoundException("Retail file not found with ID: " + id);
    }
}