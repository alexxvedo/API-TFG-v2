package com.example.api_v2.repository;

import com.example.api_v2.model.FlashcardReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlashcardReviewRepository extends JpaRepository<FlashcardReview, Long> {
    List<FlashcardReview> findByFlashcardId(Long flashcardId);
}