package com.example.movieticket.util;
import com.example.movieticket.common.TokenType;
import com.example.movieticket.common.UserRole;
import com.example.movieticket.model.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected long REFRESH_DURATION;
    public String generateToken(User user, long durationSeconds, String tokenType) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet =new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("My domain") //website's domain
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(durationSeconds, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", tokenType)
                .claim("roles", buildRole(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        return jwsObject.serialize();
    }
    public String generateAccessToken(User user) {
        return generateToken(user, VALID_DURATION, String.valueOf(TokenType.ACCESS));
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_DURATION, String.valueOf(TokenType.REFRESH));
    }
    private String buildRole(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        UserRole userRole = user.getRole();
        if(userRole != null) {
            stringJoiner.add(user.getRole().toString());
        }
        return "ROLE_" + stringJoiner;
    }
}