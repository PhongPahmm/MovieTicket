package com.example.movieticket.configuration;

import com.example.movieticket.common.UserRole;
import com.example.movieticket.model.User;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        System.out.println("✅ Google Login Successful");
        System.out.println("📧 Email from Google: " + email);

        var user = userRepository.findByEmail(email);
        if (user == null) {
           user = new User();
           user.setEmail(email);
           user.setRole(UserRole.USER);
           user.setUsername(name);
           user.setActive(true);
           userRepository.save(user);
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        System.out.println("✅ Access Token generated: " + accessToken);
        System.out.println("✅ Refresh Token generated: " + refreshToken);

        // Redirect to frontend with token
        String redirectUrl = "http://localhost:5173/oauth2/success?token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        System.out.println("url"+ redirectUrl);
        response.sendRedirect(redirectUrl);

    }
}

