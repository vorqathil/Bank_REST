package com.example.bankcards.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class AuthenticationDTO {
    @NotEmpty()
    @Size(min = 2, max = 32, message = "Имя пользователя должно иметь длину от 2 до 32 символов")
    private String username;

    @NotEmpty()
    @Size(min = 4, max = 64, message = "Пароль должен быть длиной от 4 до 64 символов")
    private String password;

    public AuthenticationDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}