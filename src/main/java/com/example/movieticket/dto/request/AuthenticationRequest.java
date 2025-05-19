package com.example.movieticket.dto.request;

import lombok.*;

@Getter
public class AuthenticationRequest {
    private String username;
    private String password;
}
