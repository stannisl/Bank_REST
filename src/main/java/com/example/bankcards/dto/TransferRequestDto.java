package com.example.bankcards.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequestDto {
    private UUID fromCardId;
    private UUID toCardId;
    private BigDecimal amount;
}


