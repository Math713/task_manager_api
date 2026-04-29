package com.matheus.task_manager_api.service;

import com.matheus.task_manager_api.dto.TaskRequest;
import com.matheus.task_manager_api.dto.TaskResponse;
import com.matheus.task_manager_api.dto.TaskUpdateRequest;
import com.matheus.task_manager_api.entity.Task;
import com.matheus.task_manager_api.entity.User;
import com.matheus.task_manager_api.enums.Role;
import com.matheus.task_manager_api.enums.TaskPriority;
import com.matheus.task_manager_api.enums.TaskStatus;
import com.matheus.task_manager_api.exception.ForbiddenActionException;
import com.matheus.task_manager_api.exception.TaskNotFoundException;
import com.matheus.task_manager_api.repository.TaskRepository;
import com.matheus.task_manager_api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Matheus", "matheus@gmail.com", "hashedPassword", Role.USER);

        task = Task.builder()
                .id(1L)
                .title("Sleep earlier")
                .description("Go to sleep at 10pm")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.LOW)
                .user(user)
                .build();
    }

    @Nested
    class CreateTasks {
        // Happy path for Create Tasks
        @Test
        void shouldCreateTaskSuccessfully() { // Criar um test que não cria :)
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {

                // Arrange
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

                // Act
                TaskRequest request = new TaskRequest("Sleep earlier", "description", TaskPriority.LOW);
                TaskResponse response = taskService.create(request);

                // Assert
                assertNotNull(response);
                assertEquals("Sleep earlier", response.title());
                assertEquals(TaskStatus.PENDING, response.status());
                verify(taskRepository).save(any(Task.class));
            }
        }

        @Test
        void shouldCreateTaskWithoutDescription() {
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);

                when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

                TaskRequest request = new TaskRequest("Sleep earlier", null, TaskPriority.LOW);
                TaskResponse response = taskService.create(request);

                assertNotNull(response);
                assertEquals("Sleep earlier", response.title());
                assertEquals(TaskStatus.PENDING, response.status());
                assertNull(response.description());
                verify(taskRepository).save(any(Task.class));
            }
        }
    }

    @Nested
    class GetAllTasksTests {
        // Happy path for Get All Tasks
        @Test
        void shouldReturnPageOfTasksForAuthenticatedUser() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                Page<Task> page = new PageImpl<>(List.of(task));

                // Arrange
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.findByUserId(eq(user.getId()), any(Pageable.class))).thenReturn(page);

                // Act
                Page<TaskResponse> response = taskService.getAll(Pageable.unpaged());

                // Assert
                assertNotNull(response);
                assertEquals(1, response.getTotalElements());
                assertEquals("Sleep earlier", response.getContent().getFirst().title());
            }
        }
    }

    @Nested
    class GetTaskByIdTests {
        // Happy path for Get Task By Id
        @Test
        void shouldGetTaskByIdSuccessfully() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                // Arrange
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.findById(any(Long.class))).thenReturn(Optional.of(task));

                // Act
                TaskResponse response = taskService.getById(1L);

                // Assert
                assertNotNull(response);
                assertEquals(1, response.id());
                assertEquals("Sleep earlier", response.title());
            }
        }

        @Test
        void shouldThrowWhenTaskNotFound() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.findById(any(Long.class))).thenReturn(Optional.empty());

                assertThrows(TaskNotFoundException.class, () -> taskService.getById(1L));
            }
        }

        @Test
        void shouldThrowWhenTaskBelongsToAnotherUser() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                User newUser = new User(2L, "Caio", "caio@gmail.com", "hashedPassword", Role.USER);

                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(newUser);
                when(taskRepository.findById(any(Long.class))).thenReturn(Optional.of(task));

                assertThrows(ForbiddenActionException.class, () -> taskService.getById(1L));
            }
        }
    }

    @Nested
    class UpdateTaskTests {
        // Happy path for Update Task
        @Test
        void shouldUpdateTaskSuccessfully() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                // Arrange
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.findById(any(Long.class))).thenReturn(Optional.of(task));
                when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

                // Act
                TaskResponse response = taskService.update(1L, new TaskUpdateRequest(
                        "Sleep earlier",
                        "Go to sleep at 10pm",
                        TaskStatus.DONE,
                        TaskPriority.MEDIUM
                ));

                // Assert
                assertNotNull(response);
                assertEquals("Sleep earlier", response.title());
                assertEquals(TaskStatus.DONE, response.status());
                assertEquals(TaskPriority.MEDIUM, response.priority());
            }
        }

        @Test
        void shouldThrowWhenTaskNotFound() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.findById(any(Long.class))).thenReturn(Optional.empty());

                assertThrows(TaskNotFoundException.class, () -> taskService.update(8L, new TaskUpdateRequest(
                        "Sleep earlier",
                        "Go to sleep at 10pm",
                        TaskStatus.DONE,
                        TaskPriority.MEDIUM
                )));

                verify(taskRepository, never()).save(any(Task.class));
            }
        }
    }

    @Nested
    class DeleteTaskTests {
        // Happy path for Delete Task
        @Test
        void shouldDeleteTaskSuccessfully() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                // Arrange
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.findById(any(Long.class))).thenReturn(Optional.of(task));

                // Act
                taskService.delete(1L);

                // Assert
                verify(taskRepository).delete(task);
            }
        }

        @Test
        void shouldThrowWhenTaskNotFoundOnDelete() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
                when(taskRepository.findById(any(Long.class))).thenReturn(Optional.empty());

                assertThrows(TaskNotFoundException.class, () -> taskService.delete(8L));

                verify(taskRepository, never()).delete(task);
            }
        }
    }
}