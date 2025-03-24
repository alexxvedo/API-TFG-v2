package com.example.api_v2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class StudySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean completed = false;

    @OneToMany(mappedBy = "studySession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flashcard> flashcard = new ArrayList<>();

    @Column(nullable = false)
    private int totalCards = 0;

    @Column(nullable = false)
    private int correctAnswers = 0;

    @Column(nullable = false)
    private int incorrectAnswers = 0;

    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
    }

    public void complete() {
        this.completed = true;
        this.endTime = LocalDateTime.now();
    }
}

