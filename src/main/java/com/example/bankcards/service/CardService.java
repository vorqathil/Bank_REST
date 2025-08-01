package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.entity.enums.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Transactional(readOnly = true)
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public void createCard(Card card, User user) {
        enrichCard(card, user);
        cardRepository.save(card);
    }

    private void enrichCard(Card card, User user) {
        card.setUser(user);
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.valueOf(0));
        card.setCardNumber(generateCardNumber());
    }

    private String generateCardNumber() {
        String cardNumber;
        do {
            cardNumber = generateRandomCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));

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
}
