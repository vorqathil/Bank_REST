package com.example.bankcards.exception;

public class CardNotReadyToBlockedException extends RuntimeException {
    public CardNotReadyToBlockedException(Long cardId) {
        super("The card is not ready to be blocked: " + cardId);
    }
}
