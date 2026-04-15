package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.English.teaching.center.entity.User;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(User.Role role);
    List<User> findByFullName(String fullName);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

    Optional<User> findByVerificationCode(String verificationCode);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);
}