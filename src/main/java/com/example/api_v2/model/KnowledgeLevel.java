package com.example.api_v2.model;

public enum KnowledgeLevel {
    MAL("MAL"),        // No se sabe la respuesta
    REGULAR("REGULAR"),    // Se sabe parcialmente
    BIEN("BIEN");        // Se sabe completamente

    private final String value;

    KnowledgeLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static KnowledgeLevel fromString(String value) {
        for (KnowledgeLevel level : KnowledgeLevel.values()) {
            if (level.value.equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("No KnowledgeLevel with value: " + value);
    }
}
