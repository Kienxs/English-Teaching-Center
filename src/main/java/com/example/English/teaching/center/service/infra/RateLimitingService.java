package com.example.English.teaching.center.service.infra;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitingService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String email){
        return buckets.computeIfAbsent(email, key -> Bucket.builder()
            .addLimit(Bandwidth.simple(3, Duration.ofMinutes(10)))
            .build());
    }
}
