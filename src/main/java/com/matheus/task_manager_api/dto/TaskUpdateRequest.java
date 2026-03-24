package com.matheus.task_manager_api.dto;

import com.matheus.task_manager_api.enums.TaskPriority;
import com.matheus.task_manager_api.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data require to update a task")
public record TaskUpdateRequest(
        @Schema(description = "Task name")
        String title,

        @Schema(description = "Task description")
        String description,

        @Schema(description = "Task status", example = "IN_PROGRESS")
        TaskStatus status,

        @Schema(description = "Task priority", example = "MEDIUM")
        TaskPriority priority
) {}