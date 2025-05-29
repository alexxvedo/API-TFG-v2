package com.example.api_v2.service;

import com.example.api_v2.dto.*;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.*;
import com.example.api_v2.model.Collection;
import com.example.api_v2.repository.*;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final CollectionRepository collectionRepository;
    private final FlashcardReviewRepository reviewRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final UserFlashcardProgressRepository userFlashcardProgressRepository;
    private final UserStatsService userStatsService;


    /**
     * Obtiene las flashcards de una colección con el progreso individual del
     * usuario especificado.
     * NO crea registros de progreso automáticamente, solo devuelve los existentes.
     * 
     * @param collectionId ID de la colección
     * @param email       ID del usuario
     * @return Lista de flashcards con su progreso individual
     */
    @Transactional(readOnly = true) // Cambiado a readOnly para evitar modificaciones
    public List<FlashcardDto> getFlashcardsByCollectionWithProgress(Long collectionId, String email) {
        // Obtener todas las flashcards de la colección
        List<Flashcard> flashcards = flashcardRepository.findByCollectionId(collectionId);

        Optional<User> user = userRepository.findByEmail(email);

        // Obtener todos los progresos existentes para este usuario y colección
        List<UserFlashcardProgress> existingProgresses = userFlashcardProgressRepository
                .findByCollectionIdAndUserId(collectionId, user.get().getId());


        // Crear un mapa para acceder rápidamente a los progresos por flashcardId
        Map<Long, UserFlashcardProgress> progressMap = existingProgresses.stream()
                .collect(Collectors.toMap(
                        progress -> progress.getFlashcard().getId(),
                        progress -> progress,
                        // En caso de duplicados, mantener el primero
                        (existing, replacement) -> existing));


        // Convertir las flashcards a DTOs con progreso individual (solo si existe)
        return flashcards.stream()
                .map(flashcard -> {
                    // Convertir la flashcard a DTO
                    FlashcardDto dto = flashcard.toDto();

                    // Buscar el progreso existente en el mapa
                    UserFlashcardProgress progress = progressMap.get(flashcard.getId());

                    // Solo añadir información de progreso si existe
                    if (progress != null) {
                        dto.setKnowledgeLevel(progress.getKnowledgeLevel());
                        dto.setRepetitionLevel(progress.getRepetitionLevel());
                        dto.setEaseFactor(progress.getEaseFactor());
                        dto.setNextReviewDate(progress.getNextReviewDate());
                        dto.setLastReviewedAt(progress.getLastReviewedAt());
                        dto.setReviewCount(progress.getReviewCount());
                        dto.setSuccessCount(progress.getSuccessCount());
                        dto.setFailureCount(progress.getFailureCount());
                        dto.setStudyTimeInSeconds(progress.getStudyTimeInSeconds());
                        dto.setReviews(progress.getReviews());

                        // Establecer el estado de la flashcard basado en el progreso del usuario
                        // Si la flashcard tiene un nextReviewDate  posterior a hoy, entonces es "completada"
                        // Si la flashcard tiene un nextReviewDate anterior a hoy o de hoy, entonces es "revision"
                        // Si la flashcard no tiene un nextReviewDate, entonces es "sinHacer"
                        //
                        if (progress.getNextReviewDate() != null && progress.getNextReviewDate().toLocalDate().isAfter(LocalDate.now())) {
                            dto.setStatus("completada");
                        } else {
                            dto.setStatus("revision");
                        }
                    } else {
                        // Si no hay progreso, establecer estado por defecto
                        dto.setStatus("sinHacer");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las flashcards que necesitan revisión, priorizadas según el algoritmo
     * SM-2 y el progreso individual del usuario
     * 
     * @param collectionId ID de la colección
     * @param userId       ID del usuario que está estudiando
     * @return Lista de flashcards para revisar, ordenadas por prioridad
     */
    @Transactional
    public List<Flashcard> getFlashcardsForReview(Long collectionId, String userId) {
        // Obtener todas las flashcards de la colección
        List<Flashcard> flashcards = flashcardRepository.findByCollectionId(collectionId);
        LocalDateTime now = LocalDateTime.now();

        // Obtener el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Lista para almacenar flashcards que necesitan revisión
        List<Flashcard> dueFlashcards = new ArrayList<>();

        // Para cada flashcard, evaluar si necesita revisión según el progreso
        // individual
        for (Flashcard flashcard : flashcards) {
            // Buscar o crear el progreso individual para esta flashcard
            UserFlashcardProgress progress = userFlashcardProgressRepository
                    .findByFlashcardIdAndUserId(flashcard.getId(), userId)
                    .orElseGet(() -> {
                        // Si no existe progreso, crear uno nuevo
                        UserFlashcardProgress newProgress = new UserFlashcardProgress();
                        newProgress.setUser(user);
                        newProgress.setFlashcard(flashcard);
                        newProgress.setCollection(flashcard.getCollection());
                        newProgress.setRepetitionLevel(0);
                        newProgress.setEaseFactor(2.5);
                        newProgress.setReviewCount(0);
                        newProgress.setSuccessCount(0);
                        newProgress.setFailureCount(0);
                        newProgress.setLastReviewedAt(null);
                        newProgress.setNextReviewDate(now); // Disponible para revisar ahora
                        return userFlashcardProgressRepository.save(newProgress);
                    });

            // Determinar si esta flashcard necesita revisión
            boolean needsReview = progress.getLastReviewedAt() == null ||
                    (progress.getNextReviewDate() != null &&
                            progress.getNextReviewDate().isBefore(now));

            if (needsReview) {
                dueFlashcards.add(flashcard);
            }
        }

        // Ordenar las flashcards por prioridad según el progreso individual
        dueFlashcards.sort((f1, f2) -> {
            UserFlashcardProgress p1 = userFlashcardProgressRepository
                    .findByFlashcardIdAndUserId(f1.getId(), userId).orElse(null);
            UserFlashcardProgress p2 = userFlashcardProgressRepository
                    .findByFlashcardIdAndUserId(f2.getId(), userId).orElse(null);

            if (p1 == null)
                return -1;
            if (p2 == null)
                return 1;

            // Criterio 1: Primero las que nunca se han revisado
            if (p1.getLastReviewedAt() == null && p2.getLastReviewedAt() != null) {
                return -1;
            }
            if (p1.getLastReviewedAt() != null && p2.getLastReviewedAt() == null) {
                return 1;
            }

            // Criterio 2: Luego por nivel de conocimiento (primero las que peor se conocen)
            if (p1.getKnowledgeLevel() != p2.getKnowledgeLevel()) {
                if (p1.getKnowledgeLevel() == null)
                    return -1;
                if (p2.getKnowledgeLevel() == null)
                    return 1;

                // Ordenar por nivel de conocimiento (MAL -> REGULAR -> BIEN)
                if (p1.getKnowledgeLevel() == KnowledgeLevel.MAL)
                    return -1;
                if (p2.getKnowledgeLevel() == KnowledgeLevel.MAL)
                    return 1;
                if (p1.getKnowledgeLevel() == KnowledgeLevel.REGULAR)
                    return -1;
                if (p2.getKnowledgeLevel() == KnowledgeLevel.REGULAR)
                    return 1;
            }

            // Criterio 3: Luego por fecha de próxima revisión (primero las más atrasadas)
            if (p1.getNextReviewDate() != null && p2.getNextReviewDate() != null) {
                return p1.getNextReviewDate().compareTo(p2.getNextReviewDate());
            }

            // Criterio 4: Luego por nivel de repetición (primero las de menor nivel)
            if (p1.getRepetitionLevel() != null && p2.getRepetitionLevel() != null) {
                return p1.getRepetitionLevel().compareTo(p2.getRepetitionLevel());
            }

            // Si todo lo demás es igual, ordenar por ID
            return f1.getId().compareTo(f2.getId());
        });

        return dueFlashcards;
    }

    public FlashcardStatsDto getFlashcardStats(Long collectionId, String email) {
        // Obtener el usuario por email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // Obtener el ID del usuario
        String userId = user.getId();

        List<UserFlashcardProgress> userProgressList = userFlashcardProgressRepository
                .findByCollectionIdAndUserId(collectionId, userId);
        FlashcardStatsDto stats = new FlashcardStatsDto();

        // Inicializar todas las estadísticas a 0 o valores predeterminados
        stats.setTotalFlashcards(0L);
        stats.setDueForReview(0L);
        stats.setCreadasHoy(0);
        stats.setCreadasUltimos7Dias(0);
        stats.setCreadasUltimos30Dias(0);
        stats.setTotalCreadas(0);
        stats.setRevisadasHoy(0);
        stats.setRevisadasUltimos7Dias(0);
        stats.setRevisadasUltimos30Dias(0);
        stats.setTotalRevisadas(0);
        stats.setReviewedFlashcards(0L);
        stats.setReviewRate(0.0);
        stats.setSuccessRate(0.0);
        stats.setPorcentajeExito(0.0);
        stats.setTiempoMedioRevision(0.0);
        stats.setPorcentajeCompletadas(0.0);
        stats.setRachaActual(user.getCurrentStreak() != null ? user.getCurrentStreak() : 0);
        stats.setRachaMasLarga(user.getBestStreak() != null ? user.getBestStreak() : 0);
        stats.setDiasTotalesEstudio(user.getTotalStudyDays() != null ? user.getTotalStudyDays() : 0);

        // Inicializar listas para estados y niveles de conocimiento
        List<FlashcardStatsDto.FlashcardStatusCount> estadosPorStatus = new ArrayList<>();
        FlashcardStatsDto.FlashcardStatusCount completada = new FlashcardStatsDto.FlashcardStatusCount();
        completada.setStatus("COMPLETADA");
        completada.setLabel("Completada");
        completada.setCount(0);
        completada.setPorcentaje(0.0);
        estadosPorStatus.add(completada);

        FlashcardStatsDto.FlashcardStatusCount revisar = new FlashcardStatsDto.FlashcardStatusCount();
        revisar.setStatus("REVISAR");
        revisar.setLabel("Revisar");
        revisar.setCount(0);
        revisar.setPorcentaje(0.0);
        estadosPorStatus.add(revisar);

        FlashcardStatsDto.FlashcardStatusCount sinHacer = new FlashcardStatsDto.FlashcardStatusCount();
        sinHacer.setStatus("SIN_HACER");
        sinHacer.setLabel("Sin hacer");
        sinHacer.setCount(0);
        sinHacer.setPorcentaje(0.0);
        estadosPorStatus.add(sinHacer);

        stats.setEstadosPorStatus(estadosPorStatus);

        List<FlashcardStatsDto.FlashcardKnowledgeCount> estadosPorConocimiento = new ArrayList<>();

        FlashcardStatsDto.FlashcardKnowledgeCount bien = new FlashcardStatsDto.FlashcardKnowledgeCount();
        bien.setKnowledgeLevel(KnowledgeLevel.BIEN);
        bien.setLabel("Bien");
        bien.setCount(0);
        bien.setPorcentaje(0.0);
        estadosPorConocimiento.add(bien);

        FlashcardStatsDto.FlashcardKnowledgeCount regular = new FlashcardStatsDto.FlashcardKnowledgeCount();
        regular.setKnowledgeLevel(KnowledgeLevel.REGULAR);
        regular.setLabel("Regular");
        regular.setCount(0);
        regular.setPorcentaje(0.0);
        estadosPorConocimiento.add(regular);

        FlashcardStatsDto.FlashcardKnowledgeCount mal = new FlashcardStatsDto.FlashcardKnowledgeCount();
        mal.setKnowledgeLevel(KnowledgeLevel.MAL);
        mal.setLabel("Mal");
        mal.setCount(0);
        mal.setPorcentaje(0.0);
        estadosPorConocimiento.add(mal);

        stats.setEstadosPorConocimiento(estadosPorConocimiento);

        // Inicializar mapas para conteos
        Map<String, Long> statusCountsMap = new HashMap<>();
        statusCountsMap.put("COMPLETADA", 0L);
        statusCountsMap.put("REVISAR", 0L);
        statusCountsMap.put("SIN_HACER", 0L);
        stats.setStatusCounts(statusCountsMap);

        Map<KnowledgeLevel, Long> knowledgeLevelCountsMap = new HashMap<>();
        knowledgeLevelCountsMap.put(KnowledgeLevel.BIEN, 0L);
        knowledgeLevelCountsMap.put(KnowledgeLevel.REGULAR, 0L);
        knowledgeLevelCountsMap.put(KnowledgeLevel.MAL, 0L);
        stats.setKnowledgeLevelCounts(knowledgeLevelCountsMap);

        if (userProgressList.isEmpty()) {
            return stats;
        }

        // Fecha actual para cálculos temporales
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOf7DaysAgo = now.toLocalDate().minusDays(7).atStartOfDay();
        LocalDateTime startOf30DaysAgo = now.toLocalDate().minusDays(30).atStartOfDay();

        long totalFlashcards = userProgressList.size();
        stats.setTotalFlashcards(totalFlashcards);

        int completadasCount = 0;
        int revisarCount = 0;
        int sinHacerCount = 0;
        int bienCount = 0;
        int regularCount = 0;
        int malCount = 0;
        long dueForReview = 0;
        long totalReviewTime = 0;
        int reviewsWithTime = 0;
        int totalRevisadas = 0;
        int creadasHoy = 0;
        int creadasUltimos7Dias = 0;
        int creadasUltimos30Dias = 0;
        int totalCreadas = 0;
        int revisadasHoy = 0;
        int revisadasUltimos7Dias = 0;
        int revisadasUltimos30Dias = 0;

        for (UserFlashcardProgress progress : userProgressList) {
            Flashcard flashcard = progress.getFlashcard();

            // Estadísticas de creación (basadas en la flashcard original)
            if (flashcard.getCreatedBy() != null && flashcard.getCreatedBy().getId().equals(user.getId())) {
                totalCreadas++;
                LocalDateTime createdAt = flashcard.getCreatedAt();
                if (createdAt != null) {
                    if (createdAt.isAfter(startOfToday)) {
                        creadasHoy++;
                    }
                    if (createdAt.isAfter(startOf7DaysAgo)) {
                        creadasUltimos7Dias++;
                    }
                    if (createdAt.isAfter(startOf30DaysAgo)) {
                        creadasUltimos30Dias++;
                    }
                }
            }

            // Estadísticas de revisión (basadas en el progreso del usuario)
            if (progress.getReviewCount() > 0) {
                totalRevisadas += progress.getReviewCount();
                // Aquí deberíamos obtener las revisiones reales para calcular las fechas
                // Para simplificar, si lastReviewedAt está presente, asumimos que se revisó.
                // Una implementación más robusta usaría FlashcardReviewRepository
                LocalDateTime lastReviewedAt = progress.getLastReviewedAt();
                if (lastReviewedAt != null) {
                    if (lastReviewedAt.isAfter(startOfToday)) {
                        revisadasHoy++;
                    }
                    if (lastReviewedAt.isAfter(startOf7DaysAgo)) {
                        revisadasUltimos7Dias++;
                    }
                    if (lastReviewedAt.isAfter(startOf30DaysAgo)) {
                        revisadasUltimos30Dias++;
                    }
                }
            }

            // Actualizar contadores por estado y nivel de conocimiento
            KnowledgeLevel knowledgeLevel = progress.getKnowledgeLevel();
            if (knowledgeLevel == KnowledgeLevel.BIEN) {
                completadasCount++;
                bienCount++;
            } else if (knowledgeLevel == KnowledgeLevel.REGULAR) {
                revisarCount++;
                regularCount++;
            } else if (knowledgeLevel == KnowledgeLevel.MAL) {
                sinHacerCount++; // Consideramos MAL como "necesita repaso" o "sin hacer" para el frontend
                malCount++;
            } else {
                sinHacerCount++; // Si no tiene nivel de conocimiento, es "sin hacer"
            }

            // Tarjetas pendientes de revisión
            if (progress.getNextReviewDate() != null && progress.getNextReviewDate().isBefore(now)) {
                dueForReview++;
            }

            // Tiempo de revisión
            if (progress.getStudyTimeInSeconds() != null && progress.getStudyTimeInSeconds() > 0) {
                totalReviewTime += progress.getStudyTimeInSeconds();
                reviewsWithTime++;
            }
        }

        // Establecer estadísticas de creación
        stats.setCreadasHoy(creadasHoy);
        stats.setCreadasUltimos7Dias(creadasUltimos7Dias);
        stats.setCreadasUltimos30Dias(creadasUltimos30Dias);
        stats.setTotalCreadas(totalCreadas);

        // Establecer estadísticas de revisión
        stats.setRevisadasHoy(revisadasHoy);
        stats.setRevisadasUltimos7Dias(revisadasUltimos7Dias);
        stats.setRevisadasUltimos30Dias(revisadasUltimos30Dias);
        stats.setTotalRevisadas(totalRevisadas);
        stats.setReviewedFlashcards((long) totalRevisadas);

        // Calcular tasas
        double successRate = totalRevisadas > 0
                ? (double) (userProgressList.stream().filter(p -> p.getKnowledgeLevel() == KnowledgeLevel.BIEN).count())
                        / totalRevisadas * 100
                : 0;
        stats.setSuccessRate(successRate);
        stats.setPorcentajeExito(successRate);

        // Calcular tiempo medio de revisión
        double tiempoMedioRevision = reviewsWithTime > 0 ? (double) totalReviewTime / reviewsWithTime : 0;
        stats.setTiempoMedioRevision(tiempoMedioRevision);

        // Establecer tarjetas pendientes de revisión
        stats.setDueForReview(dueForReview);

        // Actualizar los mapas con los contadores
        statusCountsMap.put("COMPLETADA", (long) completadasCount);
        statusCountsMap.put("REVISAR", (long) revisarCount);
        statusCountsMap.put("SIN_HACER", (long) sinHacerCount);

        knowledgeLevelCountsMap.put(KnowledgeLevel.BIEN, (long) bienCount);
        knowledgeLevelCountsMap.put(KnowledgeLevel.REGULAR, (long) regularCount);
        knowledgeLevelCountsMap.put(KnowledgeLevel.MAL, (long) malCount);

        // Actualizar las estadísticas por estado con los valores reales
        for (FlashcardStatsDto.FlashcardStatusCount statusCount : stats.getEstadosPorStatus()) {
            String status = statusCount.getStatus();
            long count = 0;

            if ("COMPLETADA".equals(status)) {
                count = completadasCount;
            } else if ("REVISAR".equals(status)) {
                count = revisarCount;
            } else if ("SIN_HACER".equals(status)) {
                count = sinHacerCount;
            }

            statusCount.setCount((int) count);
            statusCount.setPorcentaje(totalFlashcards > 0 ? (double) count / totalFlashcards * 100 : 0);
        }

        // Actualizar las estadísticas por nivel de conocimiento con los valores reales
        for (FlashcardStatsDto.FlashcardKnowledgeCount knowledgeCount : stats.getEstadosPorConocimiento()) {
            KnowledgeLevel level = knowledgeCount.getKnowledgeLevel();
            long count = 0;

            if (level == KnowledgeLevel.BIEN) {
                count = bienCount;
            } else if (level == KnowledgeLevel.REGULAR) {
                count = regularCount;
            } else if (level == KnowledgeLevel.MAL) {
                count = malCount;
            }

            knowledgeCount.setCount((int) count);
            knowledgeCount.setPorcentaje(totalFlashcards > 0 ? (double) count / totalFlashcards * 100 : 0);
        }

        // Calcular porcentaje de completadas
        double porcentajeCompletadas = totalFlashcards > 0 ? (double) completadasCount / totalFlashcards * 100 : 0;
        stats.setPorcentajeCompletadas(porcentajeCompletadas);

        return stats;
    }

    @Transactional
    public FlashcardDto createFlashcard(Long collectionId, FlashcardDto flashcardDto, String email) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        User user = userRepository.findByEmail(email).orElse(null);

        Flashcard flashcard = Flashcard.builder()
                .question(flashcardDto.getQuestion())
                .answer(flashcardDto.getAnswer())
                .collection(collection)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        final Flashcard savedFlashcard = flashcardRepository.save(flashcard);

        // Actualizar estadísticas del usuario
        UserStatsDto updatedStats = new UserStatsDto();
        updatedStats.setCreatedFlashcards(1);
        updatedStats.setExperience(5);
        userStatsService.updateUserStats(user.getId().toString(), updatedStats);

        // Crear un UserFlashcardProgress para todos los usuarios del workspace
        List<UserFlashcardProgress> userFlashcardProgressList = collection.getWorkspace().getWorkspaceUsers().stream()
                .map(WorkspaceUser::getUser)
                .map(u -> {
                    UserFlashcardProgress userFlashcardProgress = UserFlashcardProgress.builder()
                            .user(u)
                            .flashcard(savedFlashcard)
                            .collection(collection)
                            .knowledgeLevel(null)
                            .repetitionLevel(0)
                            .easeFactor(2.5) // Valor por defecto
                            .nextReviewDate(LocalDateTime.now())
                            .lastReviewedAt(LocalDateTime.now())
                            .reviewCount(0)
                            .successCount(0)
                            .failureCount(0)
                            .reviews(new ArrayList<>())
                            .studyTimeInSeconds(0)
                            .build();
                    userFlashcardProgressRepository.save(userFlashcardProgress);
                    return userFlashcardProgress;
                }).collect(Collectors.toList());

        savedFlashcard.setUserFlashcardProgress(userFlashcardProgressList);

        flashcardRepository.save(savedFlashcard);

        // Forzar la inicialización del objeto para evitar que sea un proxy
        Hibernate.initialize(flashcard.getCreatedBy());
        Hibernate.initialize(flashcard.getUserFlashcardProgress());

        return convertToDto(flashcard, flashcard.getUserFlashcardProgress().get(0));

    }

    @Transactional
    public FlashcardDto updateFlashcard(Long flashcardId, FlashcardDto flashcardDto) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        updateFlashcardFromDto(flashcard, flashcardDto);
        flashcard.setUpdatedAt(LocalDateTime.now());


        return flashcardRepository.save(flashcard).toDto();
    }

    public void deleteFlashcard(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));

        flashcardRepository.delete(flashcard);
    }

    /**
     * Procesa una revisión de flashcard y actualiza el progreso del usuario
     * 
     * @param flashcardId ID de la flashcard
     * @param reviewDto   Datos de la revisión
     * @param userId      ID del usuario que realiza la revisión
     * @return La flashcard con su progreso actualizado
     */
    @Transactional
    public Flashcard processReview(Long flashcardId, FlashcardReviewDto reviewDto, String userId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        // Obtener el usuario que realiza la revisión
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Crear y guardar la revisión
        FlashcardReview review = FlashcardReview.builder()
                .flashcard(flashcard)
                .result(reviewDto.getResult())
                .timeSpentMs(reviewDto.getTimeSpentMs())
                .reviewedAt(LocalDateTime.now())
                .userId(userId) // Asignar el userId al objeto FlashcardReview
                .build();

        reviewRepository.save(review);

        // Actualizar el progreso del usuario para esta flashcard
        updateUserFlashcardProgress(user, flashcard, reviewDto.getResult(), reviewDto.getTimeSpentMs());

        return flashcard;
    }

    /**
     * Actualiza el progreso individual del usuario para una flashcard específica
     * 
     * @param user        El usuario que realiza la revisión
     * @param flashcard   La flashcard revisada
     * @param result      El resultado de la revisión (WRONG, PARTIAL, CORRECT)
     * @param timeSpentMs Tiempo empleado en la revisión en milisegundos
     */
    @Transactional
    public void updateUserFlashcardProgress(User user, Flashcard flashcard, String result, Long timeSpentMs) {
        LocalDateTime now = LocalDateTime.now();

        // Buscar el progreso existente o crear uno nuevo
        UserFlashcardProgress progress = userFlashcardProgressRepository
                .findByFlashcardIdAndUserId(flashcard.getId(), user.getId())
                .orElseGet(() -> {
                    UserFlashcardProgress newProgress = new UserFlashcardProgress();
                    newProgress.setUser(user);
                    newProgress.setFlashcard(flashcard);
                    newProgress.setCollection(flashcard.getCollection());
                    newProgress.setRepetitionLevel(0);
                    newProgress.setEaseFactor(2.5);
                    newProgress.setReviewCount(0);
                    newProgress.setSuccessCount(0);
                    newProgress.setFailureCount(0);
                    newProgress.setLastReviewedAt(null);
                    newProgress.setNextReviewDate(now);
                    return newProgress;
                });

        // Actualizar la fecha de última revisión
        progress.setLastReviewedAt(now);

        // Convertir el resultado a una calificación de 0-5 (formato SM-2)
        int quality;
        switch (result) {
            case "MAL":
                quality = 0;
                progress.setKnowledgeLevel(KnowledgeLevel.MAL);
                progress.setFailureCount(progress.getFailureCount() + 1);
                break;
            case "REGULAR":
                quality = 3;
                progress.setKnowledgeLevel(KnowledgeLevel.REGULAR);
                break;
            case "BIEN":
                quality = 5;
                progress.setKnowledgeLevel(KnowledgeLevel.BIEN);
                progress.setSuccessCount(progress.getSuccessCount() + 1);
                break;
            default:
                quality = 0;
                progress.setKnowledgeLevel(KnowledgeLevel.MAL);
                break;
        }

        // Incrementar contador de revisiones
        progress.setReviewCount(progress.getReviewCount() + 1);

        // Aplicar algoritmo SM-2
        if (quality < 3) {
            // Si la respuesta es mala, reiniciar
            progress.setRepetitionLevel(0);
            progress.setNextReviewDate(now.plusHours(1)); // Revisar en 1 hora
        } else {
            // Calcular nuevo factor de facilidad (EF)
            double oldEF = progress.getEaseFactor();
            double newEF = Math.max(1.3, oldEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));
            progress.setEaseFactor(newEF);

            // Calcular próximo intervalo
            int repetitionLevel = progress.getRepetitionLevel();
            int interval;

            if (repetitionLevel == 0) {
                interval = 1; // 1 día
            } else if (repetitionLevel == 1) {
                interval = 6; // 6 días
            } else {
                // Para niveles mayores, multiplicar por el factor de facilidad
                interval = Math.round((float) (repetitionLevel * progress.getEaseFactor()));
            }

            // Incrementar nivel de repetición
            progress.setRepetitionLevel(repetitionLevel + 1);
            progress.setNextReviewDate(now.plusDays(interval));
        }

        // Actualizar tiempo de estudio
        if (timeSpentMs != null) {
            Integer studyTimeInSeconds = progress.getStudyTimeInSeconds();
            if (studyTimeInSeconds == null) {
                studyTimeInSeconds = 0;
            }
            progress.setStudyTimeInSeconds(studyTimeInSeconds + (int) (timeSpentMs / 1000));
        }

        // Guardar el progreso
        userFlashcardProgressRepository.save(progress);

        // Actualizar rachas de estudio del usuario
        updateUserStudyStreaks(user, flashcard.getCollection());
    }

    /**
     * Actualiza las rachas de estudio del usuario
     * 
     * @param user       El usuario que realiza la revisión
     * @param collection La colección a la que pertenece la flashcard
     */
    private void updateUserStudyStreaks(User user, Collection collection) {
        LocalDate today = LocalDate.now();
        LocalDate lastStudyDate = user.getLastStudyDate();

        // Si es el primer estudio del día o no hay fecha previa
        if (lastStudyDate == null || !today.equals(lastStudyDate)) {
            // Incrementar total de días de estudio
            Integer totalStudyDays = user.getTotalStudyDays();
            if (totalStudyDays == null) {
                totalStudyDays = 0;
            }
            user.setTotalStudyDays(totalStudyDays + 1);

            // Actualizar racha actual
            Integer currentStreak = user.getCurrentStreak();
            if (currentStreak == null) {
                currentStreak = 0;
            }

            // Si la última fecha de estudio fue ayer, incrementar racha
            if (lastStudyDate == null || today.minusDays(1).equals(lastStudyDate)) {
                currentStreak++;
            } else if (!today.equals(lastStudyDate)) {
                // Si no fue ayer ni hoy, reiniciar racha
                currentStreak = 1;
            }

            user.setCurrentStreak(currentStreak);
            user.setLastStudyDate(today);

            // Actualizar mejor racha
            Integer bestStreak = user.getBestStreak();
            if (bestStreak == null || currentStreak > bestStreak) {
                user.setBestStreak(currentStreak);
            }

            // Guardar el usuario actualizado
            userRepository.save(user);
        }
    }

    // El método updateCollectionStatistics ha sido eliminado ya que las
    // estadísticas
    // ahora son individuales por usuario y se almacenan en UserFlashcardProgress


    private void updateFlashcardFromDto(Flashcard flashcard, FlashcardDto flashcardDto) {
        if (flashcardDto.getQuestion() != null) {
            flashcard.setQuestion(flashcardDto.getQuestion());
        }
        if (flashcardDto.getAnswer() != null) {
            flashcard.setAnswer(flashcardDto.getAnswer());
        }
        UserFlashcardProgress userFlashcardProgress = userFlashcardProgressRepository.findByFlashcardIdAndUserId(flashcard.getId(), flashcardDto.getCreatedBy().getId())
                .orElse(null);
        if (userFlashcardProgress != null) {
            userFlashcardProgress.setEaseFactor(2.5);
            userFlashcardProgress.setRepetitionLevel(0);
            userFlashcardProgress.setNextReviewDate(LocalDateTime.now());
            userFlashcardProgress.setStudyTimeInSeconds(0);
            userFlashcardProgressRepository.save(userFlashcardProgress);
        }
    }

    private FlashcardDto convertToDto(Flashcard flashcard, UserFlashcardProgress userFlashcardProgress) {
        FlashcardDto dto = new FlashcardDto();
        dto.setId(flashcard.getId());
        dto.setQuestion(flashcard.getQuestion());
        dto.setAnswer(flashcard.getAnswer());
        dto.setCollectionId(flashcard.getCollection().getId());
        dto.setKnowledgeLevel(userFlashcardProgress.getKnowledgeLevel());
        dto.setNextReviewDate(userFlashcardProgress.getNextReviewDate());
        dto.setLastReviewedAt(userFlashcardProgress.getLastReviewedAt());
        if (userFlashcardProgress.getNextReviewDate().toLocalDate() != null && userFlashcardProgress.getNextReviewDate().toLocalDate().isAfter(LocalDate.now())) {
            dto.setStatus("completada");
        } else if(userFlashcardProgress.getNextReviewDate() == null) {
            dto.setStatus("sinHacer");
        } else {
            dto.setStatus("revision");
        }
        dto.setRepetitionLevel(userFlashcardProgress.getRepetitionLevel());
        dto.setEaseFactor(userFlashcardProgress.getEaseFactor());
        dto.setReviews(userFlashcardProgress.getReviews());
        dto.setReviewCount(userFlashcardProgress.getReviewCount());
        dto.setSuccessCount(userFlashcardProgress.getSuccessCount());
        dto.setFailureCount(userFlashcardProgress.getFailureCount());
        return dto;
    }

    public FlashcardDto convertToDto(Flashcard flashcard, String userId) {
        Optional<UserFlashcardProgress> userFlashcardProgress = userFlashcardProgressRepository.findByFlashcardIdAndUserId(flashcard.getId(), userId);
        FlashcardDto dto = new FlashcardDto();
        dto.setId(flashcard.getId());
        dto.setQuestion(flashcard.getQuestion());
        dto.setAnswer(flashcard.getAnswer());
        dto.setCollectionId(flashcard.getCollection().getId());
        dto.setKnowledgeLevel(userFlashcardProgress.get().getKnowledgeLevel());
        dto.setNextReviewDate(userFlashcardProgress.get().getNextReviewDate());
        dto.setLastReviewedAt(userFlashcardProgress.get().getLastReviewedAt());
        if (userFlashcardProgress.get().getNextReviewDate() != null && userFlashcardProgress.get().getNextReviewDate().toLocalDate().isAfter(LocalDate.now())) {
            dto.setStatus("completada");
        } else if(userFlashcardProgress.get().getNextReviewDate() == null) {
            dto.setStatus("sinHacer");
        } else {
            dto.setStatus("revision");
        }
        dto.setRepetitionLevel(userFlashcardProgress.get().getRepetitionLevel());
        dto.setEaseFactor(userFlashcardProgress.get().getEaseFactor());
        dto.setReviews(userFlashcardProgress.get().getReviews());
        dto.setReviewCount(userFlashcardProgress.get().getReviewCount());
        dto.setSuccessCount(userFlashcardProgress.get().getSuccessCount());
        dto.setFailureCount(userFlashcardProgress.get().getFailureCount());
        return dto;
    }



}
