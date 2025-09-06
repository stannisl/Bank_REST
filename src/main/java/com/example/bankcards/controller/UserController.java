package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.dto.UpdateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Операции с юзерами для админов")
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список пользователей (Admin only)")
    @Parameter(name = "page", description = "страница")
    @Parameter(name = "size", description = "кол-во юзеров на 1ой странице")
    @ApiResponse(responseCode = "200", description = "Пользователи успешно получены")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getUsers(page,size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователя (Admin only)")
    @Parameter(name = "uuid", description = "UUID пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно получен")
    public ResponseEntity<UserDto> getUser(
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать пользователя (Admin only)")
    @Parameter(name = "uuid", description = "CreateUserDto")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан")
    public ResponseEntity<UserDto> createUser(
            @RequestBody @Valid CreateUserDto userDto
    ) {
        return ResponseEntity.status(201).body(userService.createUser(userDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить пользователя (Admin only)")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserDto userDto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя (Admin only)")
    public ResponseEntity<MessageDto> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        MessageDto msg = new MessageDto();
        msg.setMessage("Пользователь удалён");
        return ResponseEntity.ok(msg);
    }
}
