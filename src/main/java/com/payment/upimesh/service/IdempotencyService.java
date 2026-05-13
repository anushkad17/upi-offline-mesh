package com.payment.upimesh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Prefix helps organize keys in Redis (e.g., idempotency:abc123hash)
    private static final String PREFIX = "idempotency:";

    @Value("${upi.mesh.idempotency-ttl-seconds:86400}")
    private long ttlSeconds;

    /**
     * Attempts to claim a hash.
     * Uses Redis SETNX (setIfAbsent) which is atomic across all instances.
     */
    public boolean claim(String packetHash) {
        String key = PREFIX + packetHash;

        // setIfAbsent returns true if the key was created (first time seen)
        // It also sets the TTL so the key expires automatically.
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key,
                Instant.now().toString(),
                ttlSeconds,
                TimeUnit.SECONDS
        );

        return Boolean.TRUE.equals(success);
    }

    /**
     * Returns the approximate number of active idempotency keys.
     */
    public long size() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }

    /**
     * Clears all mesh packet hashes from Redis.
     */
    public void clear() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /* NOTE: evictExpired() is REMOVED.
       Redis handles TTL natively, so we don't need a background thread
       constantly cleaning up the map.
    */
}