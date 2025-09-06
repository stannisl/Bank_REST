package com.example.bankcards.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CardDto {
    private UUID uuid;
    private String numberMasked;
    private String owner;
    private String expiryDate;
    private String status;
    private BigDecimal balance;
}
