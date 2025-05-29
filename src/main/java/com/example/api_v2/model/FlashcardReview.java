package com.example.api_v2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    @JsonBackReference
    private Flashcard flashcard;

    @Column(nullable = false)
    private String result;  // WRONG, PARTIAL, CORRECT

    @Column(name = "time_spent_ms")
    private Long timeSpentMs;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
    
    @Column(name = "user_id")
    private String userId;
}
