package com.example.bankcards.exception;

import com.example.bankcards.dto.MessageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<MessageDto> handleNotFound(NotFoundException ex) {
        MessageDto msg = new MessageDto();
        msg.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageDto> handleBadRequest(IllegalArgumentException ex) {
        MessageDto msg = new MessageDto();
        msg.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageDto> handleAccessDenied(AccessDeniedException ex) {
        MessageDto msg = new MessageDto();
        msg.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageDto> handleGeneric(Exception ex) {
        MessageDto msg = new MessageDto();
        msg.setMessage("Internal server error");

        //Todo: логировать ex

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}
