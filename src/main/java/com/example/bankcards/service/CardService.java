package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotActivatedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNotReadyToBlockedException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.entity.enums.Status;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final ModelMapper modelMapper;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CardNumberGenerator cardNumberGenerator, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardNumberGenerator = cardNumberGenerator;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public void createCard(String username, BigDecimal balance) {
        Card card = new Card();
        card.setUser(userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("Username not found!")));
        card.setBalance(balance);
        enrichCard(card);
        cardRepository.save(card);
    }

    public List<CardDTO> getCards() {
        return cardRepository.findAll().stream().map(this::convertToCardDTO).collect(Collectors.toList());
    }

    public Page<CardDTO> getCards(User user, String search, Pageable pageable) {
        Page<Card> cardPage;

        if (search != null && !search.trim().isEmpty()) {
            cardPage = cardRepository.findAllByUserAndCardNumberContainingIgnoreCase(user, search.trim(), pageable);
        } else {
            cardPage = cardRepository.findAllByUser(user, pageable);
        }

        return cardPage.map(this::convertToCardDTO);
    }

    public CardDTO getCard(Long cardId) {
        return convertToCardDTO(cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId)));
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if(!card.getStatus().equals(Status.PENDING_TO_BLOCKING)) {
            throw new CardNotReadyToBlockedException(cardId);
        }
        card.setStatus(Status.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void blockCard(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if(!card.getUser().equals(user)) {
            throw new CardNotFoundException(cardId);
        }
        card.setStatus(Status.PENDING_TO_BLOCKING);
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

    public BigDecimal getBalance(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getUser().equals(user)) {
            throw new CardNotFoundException(cardId);
        }
        return card.getBalance();
    }

    @Transactional
    public void transfer(Long cardId, String recipientCardNumber, double amount, User user) {
        Card senderCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        Card recipientCard = cardRepository.findByCardNumber(recipientCardNumber)
                .orElseThrow(() -> new CardNotFoundException(recipientCardNumber));
        if (!senderCard.getUser().equals(user) || !recipientCard.getUser().equals(user)) {
            throw new CardNotFoundException("None of the cards belong to you");
        }

        if (senderCard.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0 || amount <= 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer");
        }

        if (senderCard.getStatus() != Status.ACTIVE || recipientCard.getStatus() != Status.ACTIVE) {
            throw new CardNotActivatedException("One or both cards are not active");
        }

        senderCard.setBalance(senderCard.getBalance().subtract(BigDecimal.valueOf(amount)));
        recipientCard.setBalance(recipientCard.getBalance().add(BigDecimal.valueOf(amount)));

        cardRepository.save(senderCard);
        cardRepository.save(recipientCard);
    }

    @Transactional
    public void updateExpirationTime() {
        LocalDateTime currentDate = LocalDateTime.now();
        List<Card> expiredCards = cardRepository.findByValidityPeriodBeforeAndStatusNot(currentDate, Status.EXPIRED);

        expiredCards.forEach(card -> card.setStatus(Status.EXPIRED));
        cardRepository.saveAll(expiredCards);
    }

    private void enrichCard(Card card) {
        card.setStatus(Status.PENDING);
        card.setCardNumber(cardNumberGenerator.generateCardNumber());
        card.setMaskedCardNumber(cardNumberGenerator.maskedCardNumber(card.getCardNumber()));
        card.setValidityPeriod(LocalDateTime.now().plusYears(2));
    }

    private CardDTO convertToCardDTO(Card card) {
        return modelMapper.map(card, CardDTO.class);
    }
}
