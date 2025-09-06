package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.MessageDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());

        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody CreateUserDto createUserDto) {
        if (userRepository.findByUsername(createUserDto.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new AuthResponse("Username already exists", null)
            );
        }

        if (userRepository.findByEmail(createUserDto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new AuthResponse("Email already exists", null)
            );
        }

        User user = new User();
        user.setFirstName(createUserDto.getFirstName());
        user.setLastName(createUserDto.getLastName());
        user.setUsername(createUserDto.getUsername());
        user.setEmail(createUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        user.setRole(UserRole.USER);

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(createUserDto.getUsername(), createUserDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, user.getRole().name()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username != null) {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                String newToken = jwtUtil.generateToken(
                        org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                                .password(user.getPassword())
                                .roles(user.getRole().name())
                                .build()
                );

                return ResponseEntity.ok(new AuthResponse(newToken, user.getRole().name()));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new AuthResponse("Invalid refresh token", null)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageDto> logout() {
        SecurityContextHolder.clearContext();
        MessageDto message = new MessageDto();
        message.setMessage("Logged out successfully");
        return ResponseEntity.ok(message);
    }
}