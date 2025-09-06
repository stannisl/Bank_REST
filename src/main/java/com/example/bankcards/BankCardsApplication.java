package com.example.bankcards;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(
        info = @Info(
                title = "Bankcards Api",
                version = "1.0",
                description = "API documentation"
        )
)
@SpringBootApplication
public class BankCardsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankCardsApplication.class, args);
    }
}
