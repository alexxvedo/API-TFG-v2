package com.example.api_v2.dto;

import com.example.api_v2.model.Document;
import lombok.Data;

@Data
public class DocumentDto {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;

    public DocumentDto(Document document) {
        this.id = document.getId();
        this.fileName = document.getFileName();
        this.fileType = document.getFileType();
        this.fileSize = document.getFileSize();
    }

    // Getters y Setters
}
