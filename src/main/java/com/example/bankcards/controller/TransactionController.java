package com.example.bankcards.controller;


import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Перевод между картами")
    public ResponseEntity<TransactionDto> transfer(@RequestBody TransferRequestDto dto) {
        return ResponseEntity.ok(transactionService.transfer(dto));
    }
}
