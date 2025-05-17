package com.example.movieticket.dto.request;

import com.example.movieticket.common.UserRole;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserUpdateRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private UserRole role;
}
