package com.example.api_v2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "\"Account\"")
@IdClass(AccountId.class) // Clave primaria compuesta
@Data
@NoArgsConstructor
public class Account {
    
    @Id
    @Column(name = "provider", nullable = false)
    private String provider;

    @Id
    @Column(name = "\"providerAccountId\"")
    private String providerAccountId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", nullable = false)
    @JsonBackReference(value = "user-accounts")
    private User user; // Relación con User

    @Column(name="type", nullable = false)
    private String type;

    @Column(name="refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name="access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name="expires_at")
    private Integer expiresAt;

    @Column(name = "token_type", columnDefinition = "TEXT")
    private String tokenType;

    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;

    @Column(name = "id_token", columnDefinition = "TEXT")
    private String idToken;

    @Column(name = "session_state", columnDefinition = "TEXT")
    private String sessionState;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"updatedAt\"")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
