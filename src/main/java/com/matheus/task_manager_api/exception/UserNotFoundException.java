package com.matheus.task_manager_api.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}