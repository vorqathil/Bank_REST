package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;

public class UserDTO {
    private String username;
    private Role role;

    public UserDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
