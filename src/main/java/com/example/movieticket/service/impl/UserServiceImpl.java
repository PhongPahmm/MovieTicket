package com.example.movieticket.service.impl;

import com.example.movieticket.common.UserRole;
import com.example.movieticket.dto.request.UserCreationRequest;
import com.example.movieticket.dto.request.UserUpdateRequest;
import com.example.movieticket.dto.response.UserResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.User;
import com.example.movieticket.repository.InvalidatedTokenRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.service.FileStorageService;
import com.example.movieticket.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    FileStorageService fileStorageService;
    InvalidatedTokenRepository invalidatedTokenRepository;
    @Override
    public UserResponse createUser(UserCreationRequest request) {
        var role = (request.getRole() != null) ?
                request.getRole() : UserRole.USER;
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .role(role)
                .dob(request.getDob())
                .build();
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if(userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_NO_EXISTED);
        }
        var savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(int userId, UserUpdateRequest request) {
        var context = getCurrentUser();
        if (!context.getRole().equals(UserRole.ADMIN) && context.getId() != userId) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var imageName = user.getUserImage();
        if (request.getUserImage() != null && !request.getUserImage().isEmpty()) {
            try {
                if (imageName != null) {
                    Path oldImagePath = Paths.get("uploads", imageName);
                    Files.deleteIfExists(oldImagePath);
                }
                imageName = fileStorageService.upload(request.getUserImage());
            } catch (IOException e) {
                throw new RuntimeException("Failed to update image: " + e.getMessage());
            }
            user.setUserImage(imageName);
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if(request.getActive() != null) {
            user.setActive(request.getActive());
        }

        var savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(int userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        var context = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =
                userRepository
                        .findByUsername(context)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(int userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        invalidatedTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeUserStatus(int userId, boolean isActive) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setActive(isActive);
        var updateUser = userRepository.save(user);
        return mapToUserResponse(updateUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .role(user.getRole())
                .userImage(user.getUserImage())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
