package com.example.movieticket.service;

import java.util.Date;

public interface TokenBlackListService {
    void blacklistToken(String jti, Date expirationTime);
    boolean isBlacklisted(String jti);
    void removeFromBlacklist(String jti);
}
