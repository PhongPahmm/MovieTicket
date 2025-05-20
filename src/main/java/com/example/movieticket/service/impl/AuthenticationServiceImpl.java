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

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;
    JwtUtil jwtUtil;
    @NonFinal
    @Value("${jwt.secret-key}")
    protected String SECRET_KEY;


    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected long REFRESH_DURATION;

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

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .domain("localhost")
                .maxAge(REFRESH_DURATION)
                .sameSite("Strict")
                .build();

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
        String username = signedToken.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .token(jti)
                .expiryTime(expirationTime)
                .user(user)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
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

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .token(jti)
                .expiryTime(expiryTime)
                .user(user)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
//                .secure(true)
                .path("/")
                .domain("localhost")
                .maxAge(REFRESH_DURATION)
                .sameSite("Strict")
                .build();

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
        var verified = signedJWT.verify(verifier);
        if (!(verified && expirationTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (invalidatedTokenRepository.existsByToken(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }
}
