package com.example.api_v2.model;

public enum PermissionType {
    OWNER,      // Puede hacer todo, incluyendo eliminar el workspace y gestionar permisos
    EDITOR,     // Puede crear/editar/eliminar colecciones y flashcards
    VIEWER      // Solo puede ver y usar las flashcards
}
