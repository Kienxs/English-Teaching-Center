package com.example.English.teaching.center.service.infra;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitingService {
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(50000) // Tối đa lưu 50k keys 
            .build();

    public Bucket resolveBucket(String actionKey, int capacity, int minutes){
        return cache.get(actionKey, key -> createNewBucket(capacity, minutes));
    }

    private Bucket createNewBucket(int capacity, int minutes) {
        // Refill.greedy: Hồi phục toàn bộ token sau x phút
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(minutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}