package com.matheus.task_manager_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data required to refresh a token")
public record RefreshRequest(
        @Schema(description = "Refresh Token")
        @NotBlank(message = "refresh token is required")
        String refreshToken
) {}