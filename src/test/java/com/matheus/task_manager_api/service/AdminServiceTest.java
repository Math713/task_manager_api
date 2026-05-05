package com.matheus.task_manager_api.service;

import com.matheus.task_manager_api.dto.TaskResponse;
import com.matheus.task_manager_api.dto.UserResponse;
import com.matheus.task_manager_api.entity.Task;
import com.matheus.task_manager_api.entity.User;
import com.matheus.task_manager_api.enums.Role;
import com.matheus.task_manager_api.enums.TaskPriority;
import com.matheus.task_manager_api.enums.TaskStatus;
import com.matheus.task_manager_api.exception.ForbiddenActionException;
import com.matheus.task_manager_api.exception.UserNotFoundException;
import com.matheus.task_manager_api.repository.TaskRepository;
import com.matheus.task_manager_api.repository.UserRepository;
import com.matheus.task_manager_api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private User admin;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Caio", "caio@gmail.com", "hashedPassword", Role.USER);

        admin = new User(2L, "Matheus", "matheus@gmail.com", "hashedPassword", Role.ADMIN);

        task = Task.builder()
                .id(1L)
                .title("Sleep earlier")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.LOW)
                .user(user)
                .build();
    }

    @Nested
    class GetAllUsersTest {
        @Test
        void shouldGetAllUsersSuccessfully() {
            var allUsers = List.of(user);

            // Arrange
            when(userRepository.findAll()).thenReturn(allUsers);

            // Act
            List<UserResponse> response = adminService.getAllUsers();

            // Assert
            assertThat(response)
                    .isNotEmpty()
                    .hasSize(1)
                    .extracting(UserResponse::name)
                    .containsExactly(user.getName());

        }
    }

    @Nested
    class GetUserByIdTests {
        // Happy path for Get User By Id
        @Test
        void shouldGetUserByIdSuccessfully() {
            // Arrange
            when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

            // Act
            UserResponse response = adminService.getUserById(1L);

            // Assert
            assertThat(response)
                    .isNotNull()
                    .extracting(UserResponse::name, UserResponse::email)
                    .containsExactly(user.getName(), user.getEmail());
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> adminService.getUserById(1L))
                    .withMessage("User not found with id: " + 1L);
        }
    }

    @Nested
    class DeleteUserTests {
        // Happy path for Delete User
        @Test
        void shouldDeleteUserSuccessfully() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)){
                // Arrange
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(admin);
                when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

                // Act
                adminService.deleteUser(1L);

                // Assert
                verify(userRepository).delete(user);
            }
        }

        @Test
        void shouldThrowWhenUserNotFoundOnDelete() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(admin);
                when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

                assertThatExceptionOfType(UserNotFoundException.class)
                        .isThrownBy(() -> adminService.deleteUser(3L))
                        .withMessage("User not found with id: " + 3L);
            }
        }

        @Test
        void shouldThrowWhenAdminDeletesOwnAccount() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(admin);
                when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(admin));

                assertThatExceptionOfType(ForbiddenActionException.class)
                        .isThrownBy(() -> adminService.deleteUser(2L))
                        .withMessage("You cannot delete your own account");
            }
        }

        @Test
        void shouldThrowWhenAdminDeletesAnotherAdmin() {
            try(MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getAuthenticatedUser).thenReturn(admin);
                User anotherAdmin = new User(3L, "Pedro", "pedro@gmail.com", "hashedPassword", Role.ADMIN);
                when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(anotherAdmin));

                assertThatExceptionOfType(ForbiddenActionException.class)
                        .isThrownBy(() -> adminService.deleteUser(3L))
                        .withMessage("You cannot delete another admin");
            }
        }
    }

    @Nested
    class GetAllTasksTest {
        @Test
        void shouldGetAllTasksSuccessfully() {
            // Arrange
            var allTasks = new PageImpl<>(List.of(task));
            when(taskRepository.findAll(any(Pageable.class))).thenReturn(allTasks);

            // Act
            Page<TaskResponse> response = adminService.getAllTasks(Pageable.unpaged());

            // Assert
            assertThat(response.getContent())
                    .extracting(TaskResponse::title)
                    .containsExactly(task.getTitle());

        }
    }
}