package com.example.api_v2.repository;

import com.example.api_v2.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCollectionId(Long collectionId);
}
