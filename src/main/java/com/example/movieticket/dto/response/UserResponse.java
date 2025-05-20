package com.example.movieticket.dto.response;

import com.example.movieticket.common.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserResponse {
    private int id;
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private String userImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRole role;
    private Boolean active;
}
