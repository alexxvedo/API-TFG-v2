package com.example.api_v2.dto;

import com.example.api_v2.model.KnowledgeLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardStatsDto {
    // Estadísticas de creación
    private int creadasHoy;
    private int creadasUltimos7Dias;
    private int creadasUltimos30Dias;
    private int totalCreadas;

    // Estadísticas de revisión
    private int revisadasHoy;
    private int revisadasUltimos7Dias;
    private int revisadasUltimos30Dias;
    private int totalRevisadas;

    // Estadísticas de estado
    private List<FlashcardStatusCount> estadosPorStatus;

    // Estadísticas de conocimiento
    private List<FlashcardKnowledgeCount> estadosPorConocimiento;

    // Estadísticas de rendimiento
    private double porcentajeCompletadas;
    private double porcentajeExito;
    private double tiempoMedioRevision;

    // Rachas y logros
    private int rachaActual;
    private int rachaMasLarga;
    private int diasTotalesEstudio;

    // Nuevas estadísticas para el sistema de repetición espaciada
    private Long totalCards;
    private Map<String, Long> statusCounts;
    private Map<KnowledgeLevel, Long> knowledgeLevelCounts;
    private Long totalFlashcards;
    private Long reviewedFlashcards;
    private Double reviewRate;
    private Double successRate;
    private Long dueForReview;
    
    // Estadísticas de actividad reciente para un usuario específico
    private Long estudiadasHoy;
    private Long estudiadas7Dias;
    private Long estudiadas30Dias;
    private Long tiempoEstudioTotal; // en segundos

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashcardStatusCount {
        private String status;
        private String label;
        private int count;
        private double porcentaje;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashcardKnowledgeCount {
        private KnowledgeLevel knowledgeLevel;
        private String label;
        private int count;
        private double porcentaje;
    }
}
