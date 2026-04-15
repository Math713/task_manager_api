package com.matheus.task_manager_api.service;

import com.matheus.task_manager_api.mapper.TaskMapper;
import com.matheus.task_manager_api.dto.TaskRequest;
import com.matheus.task_manager_api.dto.TaskResponse;
import com.matheus.task_manager_api.dto.TaskUpdateRequest;
import com.matheus.task_manager_api.entity.Task;
import com.matheus.task_manager_api.entity.User;
import com.matheus.task_manager_api.enums.TaskStatus;
import com.matheus.task_manager_api.exception.ForbiddenActionException;
import com.matheus.task_manager_api.exception.TaskNotFoundException;
import com.matheus.task_manager_api.repository.TaskRepository;
import com.matheus.task_manager_api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse create(TaskRequest request) {
        User user = SecurityUtils.getAuthenticatedUser();

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TaskStatus.PENDING)
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponse(savedTask);
    }

    public Page<TaskResponse> getAll(Pageable pageable) {
        User user = SecurityUtils.getAuthenticatedUser();

        Page<Task> tasksByUser = taskRepository.findByUserId(user.getId(), pageable);

        return tasksByUser.map(TaskMapper::toResponse);
    }

    public TaskResponse getById(Long id) {
        User user = SecurityUtils.getAuthenticatedUser();

        Task task = findTaskByIdAndUser(id, user);

        return TaskMapper.toResponse(task);
    }

    public TaskResponse update(Long id, TaskUpdateRequest updateRequest) {
        User user = SecurityUtils.getAuthenticatedUser();

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

        return TaskMapper.toResponse(taskRepository.save(task));
    }

    public void delete(Long id) {
        User user = SecurityUtils.getAuthenticatedUser();
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
}