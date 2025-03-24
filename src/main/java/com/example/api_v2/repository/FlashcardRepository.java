package com.example.api_v2.repository;

import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    @Query("SELECT f FROM Flashcard f WHERE f.collection.id = :collectionId")
    List<Flashcard> findByCollectionId(@Param("collectionId") Long collectionId);

    List<Flashcard> findByCollection(Collection collection);

    @Query("SELECT f FROM Flashcard f WHERE f.collection.id = :collectionId AND f.nextReviewDate <= :now")
    List<Flashcard> findDueForReview(@Param("collectionId") Long collectionId, @Param("now") LocalDateTime now);



    @Query("SELECT f FROM Flashcard f " +
       "JOIN f.userFlashcardProgress p " +
       "WHERE f.collection.id = :collectionId " +
       "AND p.nextReviewDate IS NOT NULL " +
       "AND CAST(p.nextReviewDate AS date) = CURRENT_DATE " +
       "AND p.user.id = :userId")
    List<Flashcard> findFlashcardsToReviewToday(@Param("collectionId") Long collectionId, @Param("userId") String userId);





}