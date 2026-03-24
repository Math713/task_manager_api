package com.matheus.task_manager_api.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}