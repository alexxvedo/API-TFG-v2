package com.example.api_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.api_v2.model.Agent;
import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    @Query(value = """
        SELECT 
            d.id,
            d.content,
            d.file_name,
            d.file_type,
            1 - (embedding <-> CAST(:queryVector AS vector)) as similarity_score
        FROM documents d
        WHERE d.collection_id = CAST(:collectionId AS BIGINT)
        ORDER BY similarity_score DESC
        LIMIT :topK
    """, nativeQuery = true)
    List<Object[]> findSimilarDocuments(@Param("collectionId") String collectionId, @Param("queryVector") String queryVector, @Param("topK") int topK);
}
