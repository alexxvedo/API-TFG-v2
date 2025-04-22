package com.example.api_v2.dto;

import lombok.Data;

@Data
public class CreateSubtaskDto {
    private String title;
    private boolean completed;
}
