package com.matheus.task_manager_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to request a login")
public record LoginRequest(
    @Schema(description = "User email", example = "joao@gmail.com")
    @NotBlank(message = "email is required")
    @Email(message = "invalid email format")
    String email,

    @Schema(description = "User password", example = "password123")
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    String password
) {}