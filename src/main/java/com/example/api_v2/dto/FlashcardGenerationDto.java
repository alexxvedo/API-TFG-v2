package com.example.api_v2.dto;

import lombok.Data;
import java.util.List;

@Data
public class FlashcardGenerationDto {
    private String userPrompt;
    private String contextPrompt;
    private List<FlashcardDto> generatedFlashcardsHistory;
}
