package com.example.bankcards.service;


import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UpdateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getUsers(int page, int size) {
        return userRepository
                .findAll(PageRequest.of(page, size))   // исправлено
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserDto createUser(CreateUserDto dto) {
        userRepository.findByUsername(dto.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Пользователь с таким username уже существует");
        });
        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        });
        User user = new User();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.USER);

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);

        return toDto(user);
    }

    public UserDto getUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found" + id)
        );

        return toDto(user);
    }


    @Transactional
    public UserDto updateUser(UUID id, UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(user.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getUsername() != null && !dto.getUsername().isBlank() && !dto.getUsername().equals(user.getUsername())) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        userRepository.delete(user);
    }

    private UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());

        return userDto;
    }
}
