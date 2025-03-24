package com.example.api_v2.repository;

import com.example.api_v2.model.UserFlashcardProgress;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, Long>{

  Optional<UserFlashcardProgress> findByFlashcardIdAndUserId(Long flashcardId, String userId);
  
   
}
