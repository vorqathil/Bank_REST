package com.example.bankcards.entity.enums;

import lombok.Getter;

@Getter
public enum Status {
    PENDING, ACTIVE, PENDING_TO_BLOCKING, BLOCKED, EXPIRED
}
