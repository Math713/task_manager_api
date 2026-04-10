package com.matheus.task_manager_api.service;

import com.matheus.task_manager_api.config.mapper.TaskMapper;
import com.matheus.task_manager_api.config.mapper.UserMapper;
import com.matheus.task_manager_api.dto.TaskResponse;
import com.matheus.task_manager_api.dto.UserResponse;
import com.matheus.task_manager_api.entity.User;
import com.matheus.task_manager_api.exception.UserNotFoundException;
import com.matheus.task_manager_api.repository.TaskRepository;
import com.matheus.task_manager_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
    }

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(TaskMapper::toResponse);
    }
}