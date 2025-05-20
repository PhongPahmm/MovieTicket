package com.example.movieticket.service;


import com.example.movieticket.dto.request.AuthenticationRequest;
import com.example.movieticket.dto.request.LogoutRequest;
import com.example.movieticket.dto.response.AuthenticationResponse;
import com.example.movieticket.dto.response.LogoutResponse;
import com.example.movieticket.dto.response.RefreshResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response);
    LogoutResponse logout(LogoutRequest request) throws ParseException, JOSEException;
    RefreshResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws ParseException, JOSEException;
}
