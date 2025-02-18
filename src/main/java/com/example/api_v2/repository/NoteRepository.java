package com.example.api_v2.repository;

import com.example.api_v2.model.Note;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByCollectionId(Long collectionId);

    void deleteByCollectionIdAndId(Long collectionId, Long id);
    
}
