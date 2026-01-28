package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.English.teaching.center.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(User.Role role);
    List<User> findByName(String name);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

}