package com.example.api_v2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id", nullable = false)
    private StudySession studySession;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String newStatus;

    @Column
    private String userAnswer;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}