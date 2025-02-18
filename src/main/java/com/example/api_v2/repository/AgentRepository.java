package com.example.api_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.api_v2.model.Agent;
import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    @Query(value = """
        SELECT document_id, text FROM documents
        WHERE collection_id = :collectionId
        ORDER BY embedding <=> :queryVector
        LIMIT :topK
    """, nativeQuery = true)
    List<Object[]> findSimilarDocuments(@Param("collectionId") String collectionId, @Param("queryVector") float[] queryVector, @Param("topK") int topK);
}
