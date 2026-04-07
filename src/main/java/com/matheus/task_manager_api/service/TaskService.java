package com.matheus.task_manager_api.service;

import com.matheus.task_manager_api.dto.TaskRequest;
import com.matheus.task_manager_api.dto.TaskResponse;
import com.matheus.task_manager_api.dto.TaskUpdateRequest;
import com.matheus.task_manager_api.entity.Task;
import com.matheus.task_manager_api.entity.User;
import com.matheus.task_manager_api.enums.TaskStatus;
import com.matheus.task_manager_api.exception.CustomSecurityException;
import com.matheus.task_manager_api.exception.ForbiddenActionException;
import com.matheus.task_manager_api.exception.TaskNotFoundException;
import com.matheus.task_manager_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse create(TaskRequest request) {
        User user = getAuthenticadedUser();

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TaskStatus.PENDING)
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    public Page<TaskResponse> getAll(Pageable pageable) {
        User user = getAuthenticadedUser();

        Page<Task> tasksByUser = taskRepository.findByUserId(user.getId(), pageable);

        return tasksByUser.map(this::toResponse);
    }

    public TaskResponse getById(Long id) {
        User user = getAuthenticadedUser();

        Task task = findTaskByIdAndUser(id, user);

        return toResponse(task);
    }

    public TaskResponse update(Long id, TaskUpdateRequest updateRequest) {
        User user = getAuthenticadedUser();

        Task task = findTaskByIdAndUser(id, user);

        if (updateRequest.title() != null) {
            task.setTitle(updateRequest.title());
        }
        if (updateRequest.description() != null) {
            task.setDescription(updateRequest.description());
        }
        if (updateRequest.priority() != null) {
            task.setPriority(updateRequest.priority());
        }
        if (updateRequest.status() != null) {
            task.setStatus(updateRequest.status());
        }

        return toResponse(taskRepository.save(task));
    }

    public void delete(Long id) {
        User user = getAuthenticadedUser();
        Task task = findTaskByIdAndUser(id, user);
        taskRepository.delete(task);
    }

    private Task findTaskByIdAndUser(Long id, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You do not have permission to access or modify this task");
        }

        return task;
    }

    private User getAuthenticadedUser() {
        Object principal = SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User user)) {
            throw new CustomSecurityException();
        }

        return user;
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreated_at(),
                task.getUpdated_at()
        );
    }
}