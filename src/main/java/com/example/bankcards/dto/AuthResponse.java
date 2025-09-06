package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String message;

    public AuthResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }
}