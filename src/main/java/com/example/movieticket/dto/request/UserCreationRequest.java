package com.example.movieticket.dto.request;

import com.example.movieticket.common.UserRole;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserCreationRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private LocalDate dob;
    private UserRole role;
}
