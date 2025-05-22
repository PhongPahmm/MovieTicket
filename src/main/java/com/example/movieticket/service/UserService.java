package com.example.movieticket.service;

import com.example.movieticket.dto.request.UserCreationRequest;
import com.example.movieticket.dto.request.UserUpdateRequest;
import com.example.movieticket.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse updateUser(int userId, UserUpdateRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(int userId);
    void deleteUser(int userId);
    UserResponse changeUserStatus(int userId, boolean isActive);
    UserResponse getCurrentUser();
}
