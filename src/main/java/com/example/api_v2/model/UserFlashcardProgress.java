package com.example.api_v2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.api_v2.dto.UserFlashcardProgressDto;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "user_flashcard_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFlashcardProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    @JsonBackReference
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonBackReference
    private Collection collection;

    @Enumerated(EnumType.STRING)
    private KnowledgeLevel knowledgeLevel;

    @Column(nullable = false)
    private Integer repetitionLevel = 0;

    @Column(nullable = false)
    private Double easeFactor = 2.5; // EF inicial

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Integer successCount = 0;

    @Column(nullable = false)
    private Integer failureCount = 0;

    @Column(name = "reviews")
    private List<LocalDateTime> reviews = new ArrayList<>();  


    
    
    


    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.lastReviewedAt = now;
        this.nextReviewDate = now;
    }



    public UserFlashcardProgressDto toDto() {
        UserFlashcardProgressDto dto = new UserFlashcardProgressDto();
        dto.setId(id);
        dto.setUserId(user.getId());
        dto.setFlashcardId(flashcard.getId());
        dto.setKnowledgeLevel(knowledgeLevel);
        dto.setRepetitionLevel(repetitionLevel);
        dto.setEaseFactor(easeFactor);
        dto.setNextReviewDate(nextReviewDate);
        dto.setLastReviewedAt(lastReviewedAt);
        dto.setReviewCount(reviewCount);
        dto.setSuccessCount(successCount);
        dto.setFailureCount(failureCount);
        dto.setReviews(reviews);
        return dto;


    }


}
