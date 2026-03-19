package com.example.English.teaching.center.repository;

import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, LongFunction>{
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
