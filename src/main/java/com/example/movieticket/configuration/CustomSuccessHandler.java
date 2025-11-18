package com.example.movieticket.configuration;

import com.example.movieticket.common.UserRole;
import com.example.movieticket.model.User;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${jwt.refresh-duration}")
    private long refreshDuration;

    /**
     * Extracts domain from frontend URL for cookie setting
     */
    private String getCookieDomain() {
        try {
            URI uri = new URI(frontendBaseUrl);
            String host = uri.getHost();
            // Remove www. prefix if present for cookie domain
            if (host != null && host.startsWith("www.")) {
                host = host.substring(4);
            }
            // For localhost, return null (browser will handle it)
            if (host != null && host.equals("localhost")) {
                return "localhost";
            }
            return host;
        } catch (URISyntaxException e) {
            System.err.println("Error parsing frontend URL: " + e.getMessage());
            return "localhost"; // fallback
        }
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        System.out.println("Google Login Successful");
        System.out.println("Email from Google: " + email);

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
        System.out.println("Access Token generated: " + accessToken);
        System.out.println("Refresh Token generated: " + refreshToken);

        // Set refresh token cookie
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(refreshDuration);
        
        // Add domain only if not localhost
        String cookieDomain = getCookieDomain();
        if (cookieDomain != null) {
            cookieBuilder.domain(cookieDomain);
        }
        
        // Determine if we're in production (HTTPS)
        boolean isProduction = frontendBaseUrl.startsWith("https://");
        if (isProduction) {
            cookieBuilder.secure(true);
            // SameSite None is required for cross-site cookies in OAuth redirects
            cookieBuilder.sameSite("None");
        } else {
            // Localhost development
            cookieBuilder.sameSite("Strict");
        }
        
        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        System.out.println("Cookie domain set to: " + cookieDomain + ", secure: " + isProduction);

        // Redirect to frontend with token
        String redirectUrl = frontendBaseUrl+"/oauth2/success?token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        System.out.println("url"+ redirectUrl);
        response.sendRedirect(redirectUrl);

    }
}

