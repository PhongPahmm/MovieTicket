package com.example.movieticket.service.impl;

import com.example.movieticket.dto.request.AuthenticationRequest;
import com.example.movieticket.dto.request.LogoutRequest;
import com.example.movieticket.dto.response.AuthenticationResponse;
import com.example.movieticket.dto.response.LogoutResponse;
import com.example.movieticket.dto.response.RefreshResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.InvalidatedToken;
import com.example.movieticket.model.User;
import com.example.movieticket.repository.InvalidatedTokenRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.service.AuthenticationService;
import com.example.movieticket.service.TokenBlackListService;
import com.example.movieticket.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.net.URI;
import java.net.URISyntaxException;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    TokenBlackListService tokenBlackListService;
    JwtUtil jwtUtil;
    @NonFinal
    @Value("${jwt.secret-key}")
    protected String SECRET_KEY;

    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected long REFRESH_DURATION;

    @NonFinal
    @Value("${frontend.base-url}")
    protected String frontendBaseUrl;

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
            // For localhost, return "localhost"
            if (host != null && host.equals("localhost")) {
                return "localhost";
            }
            return host;
        } catch (URISyntaxException e) {
            System.err.println("Error parsing frontend URL: " + e.getMessage());
            return "localhost"; // fallback
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!user.getActive()) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }
        var accessToken = jwtUtil.generateAccessToken(user);
        var refreshToken = jwtUtil.generateRefreshToken(user);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(REFRESH_DURATION);
        
        // Add domain only if not null
        String cookieDomain = getCookieDomain();
        if (cookieDomain != null) {
            cookieBuilder.domain(cookieDomain);
        }
        
        // Determine if we're in production (HTTPS)
        boolean isProduction = frontendBaseUrl.startsWith("https://");
        if (isProduction) {
            cookieBuilder.secure(true);
            cookieBuilder.sameSite("None");
        } else {
            // Localhost development
            cookieBuilder.sameSite("Strict");
        }
        
        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }


    @Override
    public LogoutResponse logout(LogoutRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken());
        String jti = signedToken.getJWTClaimsSet().getJWTID();
        Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();
        tokenBlackListService.blacklistToken(jti, expirationTime);
        return LogoutResponse.builder().logout(true).build();
    }

    @Override
    public RefreshResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        var signJWT = verifyToken(refreshToken);
        String tokenType = signJWT.getJWTClaimsSet().getStringClaim("type");
        if (!"REFRESH".equals(tokenType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String jti = signJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();
        String username = signJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));


        tokenBlackListService.blacklistToken(jti, expiryTime);

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(REFRESH_DURATION);
        
        // Add domain only if not null
        String cookieDomain = getCookieDomain();
        if (cookieDomain != null) {
            cookieBuilder.domain(cookieDomain);
        }
        
        // Determine if we're in production (HTTPS)
        boolean isProduction = frontendBaseUrl.startsWith("https://");
        if (isProduction) {
            cookieBuilder.secure(true);
            cookieBuilder.sameSite("None");
        } else {
            // Localhost development
            cookieBuilder.sameSite("Strict");
        }
        
        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return RefreshResponse.builder()
                .newAccessToken(newAccessToken)
                .newRefreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }

    private SignedJWT verifyToken(String token)
            throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expirationTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (tokenBlackListService.isBlacklisted(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }
}
