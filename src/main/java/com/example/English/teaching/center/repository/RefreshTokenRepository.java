package com.example.English.teaching.center.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserOrderByExpiryDateAsc(User user);

    void deleteByUser(User user);
}
