package com.example.bankcards.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
}
