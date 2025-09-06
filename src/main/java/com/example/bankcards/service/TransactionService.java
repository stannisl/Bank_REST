package com.example.bankcards.service;


import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionDto transfer(TransferRequestDto dto) {
        Card fromCard = cardRepository.findById(dto.getFromCardId())
                .orElseThrow(() -> new RuntimeException("From card not found"));
        Card toCard = cardRepository.findById(dto.getToCardId())
                .orElseThrow(() -> new RuntimeException("To card not found"));

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("From card not active");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("To card not active");
        }
        if (fromCard.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Списать и зачислить
        fromCard.setBalance(fromCard.getBalance().subtract(dto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(dto.getAmount()));

        // Создать запись транзакции
        Transaction tx = new Transaction();
        tx.setFromCard(fromCard);
        tx.setToCard(toCard);
        tx.setAmount(dto.getAmount());
        tx.setTimestamp(LocalDateTime.now());

        transactionRepository.save(tx);

        // Сохранить изменения карт
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        return toDto(tx);
    }

    private TransactionDto toDto(Transaction tx) {
        TransactionDto dto = new TransactionDto();
        dto.setId(tx.getId());
        dto.setFromCardMasked(mask(tx.getFromCard().getNumberEncrypted()));
        dto.setToCardMasked(mask(tx.getToCard().getNumberEncrypted()));
        dto.setAmount(tx.getAmount());
        dto.setTimestamp(tx.getTimestamp().toString());
        return dto;
    }

    private String mask(String encrypted) {
        // можно использовать CardService.decrypt + mask
        return "**** " + encrypted.substring(encrypted.length()-4);
    }
}
