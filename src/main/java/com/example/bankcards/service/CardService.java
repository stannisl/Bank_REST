package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    private static final String AES_KEY = "0123456789abcdef"; // 16 bytes key (demo only)
    private static final SecureRandom RANDOM = new SecureRandom();

    public List<CardDto> getCards(int page, int size, String status) {
        if (status != null && !status.isBlank() && !"false".equalsIgnoreCase(status)) {
            return cardRepository
                    .findAllByStatus(CardStatus.valueOf(status.toUpperCase()), PageRequest.of(page, size))
                    .map(this::toDto)
                    .toList();
        }
        return cardRepository.findAll(PageRequest.of(page, size))
                .map(this::toDto)
                .toList();
    }

    public CardDto createCard(CardCreateDto dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getOwnerId()));

        Card card = new Card();
        card.setOwner(owner);
        card.setExpiryDate(LocalDate.parse(dto.getExpiryDate()));
        String plainNumber = generateCardNumber();
        card.setNumberEncrypted(encrypt(plainNumber));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(0.0));

        return toDto(cardRepository.save(card));
    }

    public void blockCard(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        checkExpiry(card);

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public void activateCard(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        checkExpiry(card);

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    public BigDecimal getBalance(UUID id) throws AccessDeniedException {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !card.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Нет доступа к карте");
        }

        return card.getBalance();
    }

    public boolean isCardOwner(UUID cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));
        return card.getOwner().getUsername().equals(username);
    }

    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setUuid(card.getId());
        dto.setOwner(card.getOwner().getUsername());
        dto.setExpiryDate(card.getExpiryDate().toString());
        dto.setStatus(card.getStatus().name());
        dto.setBalance(card.getBalance());
        String decrypted = decrypt(card.getNumberEncrypted());
        dto.setNumberMasked(mask(decrypted));
        return dto;
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append('4');
        for (int i = 1; i < 16; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private String mask(String plainNumber) {
        if (plainNumber == null || plainNumber.length() < 4) return "****";
        String last4 = plainNumber.substring(plainNumber.length() - 4);
        return "**** **** **** " + last4;
    }

    // === simple AES-GCM encrypt/decrypt from ai ===
    private String encrypt(String plain) {
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] cipherText = cipher.doFinal(plain.getBytes());
            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv);
            bb.put(cipherText);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    private String decrypt(String encrypted) {
        try {
            byte[] all = Base64.getDecoder().decode(encrypted);
            ByteBuffer bb = ByteBuffer.wrap(all);
            byte[] iv = new byte[12];
            bb.get(iv);
            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain);
        } catch (Exception e) {
            // todo: log error
            return null;
        }
    }

    private void checkExpiry(Card card) {
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            throw new RuntimeException("Card expired");
        }
    }
}
