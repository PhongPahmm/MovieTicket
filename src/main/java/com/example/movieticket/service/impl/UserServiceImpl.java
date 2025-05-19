package com.example.movieticket.service.impl;

import com.example.movieticket.common.UserRole;
import com.example.movieticket.dto.request.UserCreationRequest;
import com.example.movieticket.dto.request.UserUpdateRequest;
import com.example.movieticket.dto.response.UserResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.User;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
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
        return null;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return List.of();
    }

    @Override
    public UserResponse getUserById(int userId) {
        return null;
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
    public void deleteUser(int userId) {

    }

    @Override
    public UserResponse changeUserStatus(int userId, boolean isActive) {
        return null;
    }
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
