package com.matheus.task_manager_api.service;

import com.matheus.task_manager_api.dto.AuthResponse;
import com.matheus.task_manager_api.dto.LoginRequest;
import com.matheus.task_manager_api.dto.RefreshRequest;
import com.matheus.task_manager_api.dto.RegisterRequest;
import com.matheus.task_manager_api.entity.User;
import com.matheus.task_manager_api.enums.Role;
import com.matheus.task_manager_api.exception.EmailAlreadyExistsException;
import com.matheus.task_manager_api.exception.InvalidTokenException;
import com.matheus.task_manager_api.exception.UserNotFoundException;
import com.matheus.task_manager_api.repository.UserRepository;
import com.matheus.task_manager_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                1L,
                "Matheus",
                "matheus@gmail.com",
                "hashedPassword",
                Role.USER);
    }

    @Nested
    class RegisterTests {
        // Happy path for Register
        @Test
        void shouldRegisterUserSuccessfully() {
            // Arrange
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User createdUser = inv.getArgument(0);
                createdUser.setId(1L);
                return createdUser;
            });
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
            when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

            // Act
            RegisterRequest request = new RegisterRequest("Matheus", "matheus@gmail.com", "password123");
            AuthResponse response = authService.register(request);

            // Assert
            assertNotNull(response);
            assertEquals("accessToken", response.accessToken());
            verify(userRepository).save(any(User.class));
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.findByEmail("matheus@gmail.com")).thenReturn(Optional.of(user));

            RegisterRequest request = new RegisterRequest("Lucas", "matheus@gmail.com", "password123");
            assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class LoginTests {
        // Happy path for Login
        @Test
        void shouldLoginSuccessfully() {
            // Arrange
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
            when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

            // Act
            LoginRequest request = new LoginRequest("matheus@gmail.com", "password123");
            AuthResponse response = authService.login(request);

            // Assert
            assertNotNull(response);
            assertEquals("accessToken", response.accessToken());
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            LoginRequest request = new LoginRequest("pedro@gmail.com", "password123");
            assertThrows(UserNotFoundException.class, () -> authService.login(request));

            verify(jwtService, never()).generateAccessToken(any());
        }
    }

    @Nested
    class RefreshTests {
        //Happy path for Refresh
        @Test
        void shouldRefreshTokenSuccessfully() {
            // Arrange
            when(jwtService.extractEmail(any())).thenReturn(user.getEmail());
            when(jwtService.isTokenValid(any(), eq(user))).thenReturn(true);
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(any())).thenReturn("accessToken");

            // Act
            RefreshRequest request = new RefreshRequest("refreshToken");
            AuthResponse response = authService.refresh(request);

            // Assert
            assertNotNull(response);
            assertEquals("accessToken", response.accessToken());
            assertEquals("refreshToken", response.refreshToken());
        }

        @Test
        void shouldThrowWhenRefreshTokenIsInvalid() {
            when(jwtService.extractEmail(any())).thenReturn(user.getEmail());
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
            when(jwtService.isTokenValid(any(), eq(user))).thenReturn(false);

            RefreshRequest request = new RefreshRequest("refreshToken");
            assertThrows(InvalidTokenException.class, () -> authService.refresh(request));

            verify(jwtService, never()).generateAccessToken(any());
        }
    }
}