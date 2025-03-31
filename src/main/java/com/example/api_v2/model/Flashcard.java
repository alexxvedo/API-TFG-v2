package com.example.api_v2.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import com.example.api_v2.dto.FlashcardDto;
import com.example.api_v2.dto.UserFlashcardProgressDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "flashcards")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonBackReference(value = "collection-flashcards")
    private Collection collection;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private KnowledgeLevel knowledgeLevel;

    @Builder.Default
    @Column(name = "repetition_level")
    private Integer repetitionLevel = 0;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Builder.Default
    @Column(name = "status")
    private String status = "active";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonBackReference(value = "user-flashcards")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Builder.Default
    @Column(name = "success_count")
    private Integer successCount = 0;

    @Builder.Default
    @Column(name = "failure_count")
    private Integer failureCount = 0;

    @Builder.Default
    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UserFlashcardProgress> userFlashcardProgress = new ArrayList<>();
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id", nullable = true)
    @JsonBackReference
    private StudySession studySession;


    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public FlashcardDto toDto() {
        FlashcardDto dto = new FlashcardDto();
        dto.setId(id);
        dto.setQuestion(question);
        dto.setAnswer(answer);
        dto.setCollectionId(collection.getId());
        dto.setKnowledgeLevel(knowledgeLevel);
        dto.setRepetitionLevel(repetitionLevel);
        dto.setNextReviewDate(nextReviewDate);
        dto.setLastReviewedAt(lastReviewedAt);
        dto.setStatus(status);
        dto.setNotes(notes);
        dto.setTags(tags);
        dto.setCreatedBy(createdBy.toDto());  // Convertimos a DTO
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        dto.setUserFlashcardProgress(userFlashcardProgress.stream().map(UserFlashcardProgress::toDto).collect(Collectors.toList()));
        return dto;
    }

}
