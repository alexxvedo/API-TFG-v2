package com.example.api_v2.repository;

import com.example.api_v2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByClerkId(String clerkId);
    boolean existsByEmail(String email);
    boolean existsByClerkId(String clerkId);
}