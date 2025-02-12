package com.example.api_v2.dto;

import jdk.jshell.Snippet;
import lombok.Data;

@Data
public class UserDto {
    private String clerkId;  // Clerk user ID
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String createdAt;
    private String updatedAt;
}

