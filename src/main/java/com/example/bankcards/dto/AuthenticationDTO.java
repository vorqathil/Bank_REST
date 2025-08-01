package com.example.bankcards.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthenticationDTO {
    @NotEmpty()
    @Size(min = 2, max = 32, message = "Имя пользователя должно иметь длину от 2 до 32 символов")
    private String username;

    @NotEmpty()
    @Size(min = 6, max = 64, message = "Пароль должен быть длиной от 6 до 64 символов")
    private String password;
}
