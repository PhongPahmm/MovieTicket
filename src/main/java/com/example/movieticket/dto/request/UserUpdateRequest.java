package com.example.movieticket.dto.request;

import com.example.movieticket.common.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private String password;
    private MultipartFile userImage;
    private Boolean active;
    private UserRole role;
}
