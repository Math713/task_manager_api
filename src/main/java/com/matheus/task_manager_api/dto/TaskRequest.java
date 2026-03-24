package com.matheus.task_manager_api.dto;

import com.matheus.task_manager_api.enums.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data required to request a task")
public record TaskRequest(
        @Schema(description = "Title name", example = "sleep earlier")
        @NotBlank(message = "title is required")
        String title,

        @Schema(description = "Task description")
        String description,

        @Schema(description = "Task priority")
        @NotNull(message = "priority is required")
        TaskPriority priority
) {}