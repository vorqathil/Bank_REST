package com.example.bankcards.util;

import com.example.bankcards.repository.CardRepository;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CardNumberGenerator {
    private final CardRepository cardRepository;

    public CardNumberGenerator(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public String generateCardNumber() {
        String cardNumber;
        do {
            cardNumber = generateRandomCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));

        cardNumber = maskedCardNumber(cardNumber);
        return cardNumber;
    }

    private String generateRandomCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }

        return cardNumber.toString();
    }

    public String maskedCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
