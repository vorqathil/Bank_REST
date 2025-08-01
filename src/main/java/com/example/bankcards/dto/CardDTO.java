package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CardDTO {
    private String maskedCardNumber;

    private User user;

    private LocalDateTime validityPeriod;

    private Status status;

    private BigDecimal balance;
}