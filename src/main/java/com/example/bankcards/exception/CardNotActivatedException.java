package com.example.bankcards.exception;

public class CardNotActivatedException extends RuntimeException {
    public CardNotActivatedException(String message) {
        super(message);
    }
}
