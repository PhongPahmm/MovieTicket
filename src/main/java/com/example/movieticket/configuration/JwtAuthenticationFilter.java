package com.example.movieticket.configuration;

import com.example.movieticket.dto.response.ErrorResponse;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.repository.InvalidatedTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final InvalidatedTokenRepository tokenRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = auth.substring(7);
            String jti = SignedJWT.parse(token).getJWTClaimsSet().getJWTID();

            if (jti != null && tokenRepo.findByToken(jti).isPresent()) {
                sendError(response, request.getRequestURI());
                return;
            }
        } catch (ParseException e) {
            sendError(response, request.getRequestURI());
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse res, String path) throws IOException {
        ErrorResponse err = ErrorResponse.builder()
                .timestamp(new Date())
                .status(ErrorCode.UNAUTHENTICATED.getErrorCode())
                .error(ErrorCode.UNAUTHENTICATED.getErrorMessage())
                .path(path)
                .build();

        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(mapper.writeValueAsString(err));
        res.flushBuffer();
    }
}
