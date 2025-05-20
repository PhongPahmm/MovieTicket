package com.example.movieticket.controller;

import com.example.movieticket.dto.request.AuthenticationRequest;
import com.example.movieticket.dto.request.LogoutRequest;
import com.example.movieticket.dto.response.AuthenticationResponse;
import com.example.movieticket.dto.response.LogoutResponse;
import com.example.movieticket.dto.response.RefreshResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/log-in")
    public ResponseData<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        return ResponseData.<AuthenticationResponse>builder()
                .data(authenticationService.authenticate(request, response))
                .message("Successfully logged in")
                .build();

    }
    @PostMapping("log-out")
    ResponseData<LogoutResponse> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        return ResponseData.<LogoutResponse>builder()
                .data(authenticationService.logout(request))
                .message("Successfully logged out")
                .build();
    }

    @PostMapping("refresh")
    ResponseData<RefreshResponse> refresh(HttpServletRequest request, HttpServletResponse response)
            throws JOSEException, ParseException {
        return ResponseData.<RefreshResponse>builder()
                .message("Successfully refreshed")
                .data(authenticationService.refreshToken(request, response))
                .build();
    }
}
