package com.example.api_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api_v2.model.UserStats;
import java.util.Optional;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
  
  Optional<UserStats> findByUserId(String userId);
  
}
