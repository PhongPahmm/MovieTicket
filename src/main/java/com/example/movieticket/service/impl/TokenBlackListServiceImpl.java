package com.example.movieticket.service.impl;

import com.example.movieticket.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlackListServiceImpl implements TokenBlackListService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    @Override
    public void blacklistToken(String jti, Date expirationTime) {
        String key = BLACKLIST_PREFIX + jti;
        long ttl = expirationTime.getTime() - System.currentTimeMillis();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    key,
                    "blacklisted",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    public boolean isBlacklisted(String jti) {
        String key = BLACKLIST_PREFIX + jti;

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void removeFromBlacklist(String jti) {
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.delete(key);
    }
}
