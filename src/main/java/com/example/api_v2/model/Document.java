package com.example.api_v2.model;

import com.example.api_v2.util.PgVector;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Data
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Column(name = "file_name", nullable = false)
    private String fileName;  // ✅ Permite buscar documentos por nombre

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] data;

    @Type(PgVector.class)
    @Column(columnDefinition = "vector(384)")
    private float[] embedding;  // 🔹 Hibernate ya puede manejar `pgvector`

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
