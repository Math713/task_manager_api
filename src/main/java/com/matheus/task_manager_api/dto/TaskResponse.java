package com.matheus.task_manager_api.dto;

import com.matheus.task_manager_api.enums.TaskPriority;
import com.matheus.task_manager_api.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}