package com.example.movieticket.dto.request;

import com.example.movieticket.common.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class UserCreationRequest implements Serializable {
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private LocalDate dob;
    private Boolean active;
    private UserRole role;
}
