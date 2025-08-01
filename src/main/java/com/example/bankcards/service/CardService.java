package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.entity.enums.Status;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CardNumberGenerator cardNumberGenerator) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardNumberGenerator = cardNumberGenerator;
    }

    @Transactional
    public void createCard(String username, BigDecimal balance) {
        Card card = new Card();
        card.setUser(userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("Username not found!")));
        card.setBalance(balance);
        enrichCard(card);
        cardRepository.save(card);
    }

    public List<Card> getCards() {
        return cardRepository.findAll();
    }

    public Card getCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        card.setStatus(Status.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        card.setStatus(Status.ACTIVE);
        cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        cardRepository.delete(card);
    }

    private void enrichCard(Card card) {
        card.setStatus(Status.PENDING);
        card.setCardNumber(cardNumberGenerator.generateCardNumber());
        card.setMaskedCardNumber(cardNumberGenerator.maskedCardNumber(card.getCardNumber()));
        card.setValidityPeriod(LocalDateTime.now().plusYears(2));
    }
}
