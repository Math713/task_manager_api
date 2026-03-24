package com.matheus.task_manager_api.dto;

import com.matheus.task_manager_api.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {}