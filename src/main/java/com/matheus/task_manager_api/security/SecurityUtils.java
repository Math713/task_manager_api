package com.matheus.task_manager_api.security;

import com.matheus.task_manager_api.entity.User;
import com.matheus.task_manager_api.exception.CustomSecurityException;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static User getAuthenticatedUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof User user)) {
            throw new CustomSecurityException();
        }
        return user;
    }
}