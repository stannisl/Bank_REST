package com.example.bankcards.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CardCreateDto {
    private UUID ownerId;
    private String expiryDate;
}
