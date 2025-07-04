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


}