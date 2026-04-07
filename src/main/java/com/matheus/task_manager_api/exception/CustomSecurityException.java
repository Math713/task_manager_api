package com.matheus.task_manager_api.exception;

public class CustomSecurityException extends RuntimeException {
    public CustomSecurityException(String message) {
        super(message);
    }

    public CustomSecurityException() {
        super("Invalid token or incorrect user");
    }
}