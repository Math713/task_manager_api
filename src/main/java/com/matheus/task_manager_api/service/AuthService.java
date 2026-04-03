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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException(request.email());
        }

        String hashPassword = passwordEncoder.encode(request.password());

        User user = new User(
                null,
                request.name(),
                request.email(),
                hashPassword,
                Role.USER
        );

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        ));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(request.email()));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshRequest request) {
      String email = jwtService.extractEmail(request.refreshToken());

      User user = userRepository.findByEmail(email)
              .orElseThrow(() -> new UserNotFoundException(email));

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            throw new InvalidTokenException();
        }

      String accessToken = jwtService.generateAccessToken(user);

      return new AuthResponse(accessToken, request.refreshToken());
    }
}