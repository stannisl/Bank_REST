package com.example.bankcards.controller;


import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Операции с банковскими картами")
public class CardController {
    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Получить список карт")
    @ApiResponse(responseCode = "200", description = "Успешно получены запрашиваемые карты")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> getCards(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10"   ) int size,
            @RequestParam(name = "status", defaultValue = "false") String status
    ) {
        return ResponseEntity.ok(cardService.getCards(page,size,status));
    }


    @PostMapping
    @Operation(summary = "Создать карту")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@RequestBody CardCreateDto dto) {
        return ResponseEntity.status(201).body(cardService.createCard(dto));
    }

    @PutMapping("/{id}/block")
    @Operation(summary = "Блокировать карту")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageDto> blockCard(@PathVariable("id") UUID id) {
        cardService.blockCard(id);
        MessageDto msg = new MessageDto();
        msg.setMessage("Карта заблокирована");
        return ResponseEntity.ok(msg);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Активировать карту")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageDto> activateCard(@PathVariable("id") UUID id) {
        cardService.activateCard(id);
        MessageDto msg = new MessageDto();
        msg.setMessage("Карта активирована");
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Получить баланс карты")
    @PreAuthorize("hasRole('ADMIN') or @cardService.isCardOwner(#id, authentication.name)")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(cardService.getBalance(id));
    }

}
