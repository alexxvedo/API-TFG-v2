package com.example.api_v2.service;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.dto.WorkspaceActivityDto;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.model.User;
import com.example.api_v2.model.WorkspaceActivity;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceActivityRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkspaceActivityService {

    private final WorkspaceActivityRepository workspaceActivityRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public List<WorkspaceActivityDto> getActivitiesByWorkspace(Long workspaceId) {
        return workspaceActivityRepository.findAllByWorkspaceIdOrderByTimestampDesc(workspaceId).stream()
                .map(WorkspaceActivity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Registra una actividad en el workspace
     */
    public void logActivity(Long workspaceId, String userEmail, String action) {
        try {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));
            
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            WorkspaceActivity activity = WorkspaceActivity.builder()
                    .workspace(workspace)
                    .user(user)
                    .action(action)
                    .build();

            workspaceActivityRepository.save(activity);
            log.debug("Activity logged: {} by {} in workspace {}", action, userEmail, workspaceId);
        } catch (Exception e) {
            log.error("Error logging activity: {} by {} in workspace {}", action, userEmail, workspaceId, e);
        }
    }

    // Métodos de conveniencia para actividades específicas
    public void logCollectionCreated(Long workspaceId, String userEmail, String collectionName) {
        logActivity(workspaceId, userEmail, "Creó la colección '" + collectionName + "'");
    }

    public void logCollectionDeleted(Long workspaceId, String userEmail, String collectionName) {
        logActivity(workspaceId, userEmail, "Eliminó la colección '" + collectionName + "'");
    }

    public void logDocumentUploaded(Long workspaceId, String userEmail, String documentName, String collectionName) {
        logActivity(workspaceId, userEmail, "Subió el documento '" + documentName + "' a '" + collectionName + "'");
    }

    public void logDocumentDeleted(Long workspaceId, String userEmail, String documentName, String collectionName) {
        logActivity(workspaceId, userEmail, "Eliminó el documento '" + documentName + "' de '" + collectionName + "'");
    }

    public void logNoteCreated(Long workspaceId, String userEmail, String noteName, String collectionName) {
        logActivity(workspaceId, userEmail, "Creó la nota '" + noteName + "' en '" + collectionName + "'");
    }

    public void logNoteDeleted(Long workspaceId, String userEmail, String noteName, String collectionName) {
        logActivity(workspaceId, userEmail, "Eliminó la nota '" + noteName + "' de '" + collectionName + "'");
    }

    public void logFlashcardsGenerated(Long workspaceId, String userEmail, int count, String collectionName) {
        logActivity(workspaceId, userEmail, "Generó " + count + " flashcards para '" + collectionName + "'");
    }

    public void logStudySessionCompleted(Long workspaceId, String userEmail, int flashcardsStudied, String collectionName) {
        logActivity(workspaceId, userEmail, "Completó sesión de estudio de " + flashcardsStudied + " flashcards en '" + collectionName + "'");
    }

    public void logTaskCreated(Long workspaceId, String userEmail, String taskTitle) {
        logActivity(workspaceId, userEmail, "Creó la tarea '" + taskTitle + "'");
    }

    public void logTaskCompleted(Long workspaceId, String userEmail, String taskTitle) {
        logActivity(workspaceId, userEmail, "Completó la tarea '" + taskTitle + "'");
    }

    public void logTaskDeleted(Long workspaceId, String userEmail, String taskTitle) {
        logActivity(workspaceId, userEmail, "Eliminó la tarea '" + taskTitle + "'");
    }

    public void logUserJoined(Long workspaceId, String userEmail, String userName) {
        logActivity(workspaceId, userEmail, "Se unió al workspace");
    }

    public void logUserLeft(Long workspaceId, String userEmail, String userName) {
        logActivity(workspaceId, userEmail, "Abandonó el workspace");
    }

    public void logAgentQuery(Long workspaceId, String userEmail, String queryType) {
        logActivity(workspaceId, userEmail, "Realizó consulta al agente: " + queryType);
    }

}