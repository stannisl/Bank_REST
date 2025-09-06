package com.example.bankcards.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionDto {
    private UUID id;
    private String fromCardMasked;
    private String toCardMasked;
    private BigDecimal amount;
    private String timestamp;
}
