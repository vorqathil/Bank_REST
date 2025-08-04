package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.Status;
import com.example.bankcards.exception.CardNotActivatedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNotReadyToBlockedException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCard_ShouldCreateCard_WhenValidInput() {
        User user = new User();
        user.setUsername("testuser");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cardNumberGenerator.generateCardNumber()).thenReturn("1234567890123456");
        when(cardNumberGenerator.maskedCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");

        cardService.createCard("testuser", new BigDecimal("1000.00"));

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> cardService.createCard("nonexistent", new BigDecimal("1000.00")));
    }

    @Test
    void getCards_ShouldReturnAllCards() {
        Card card = new Card();
        card.setId(1L);

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);

        when(cardRepository.findAll()).thenReturn(List.of(card));
        when(modelMapper.map(card, CardDTO.class)).thenReturn(cardDTO);

        List<CardDTO> result = cardService.getCards();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void getCards_ShouldReturnPagedCards_WhenUserHasCards() {
        User user = new User();
        user.setUsername("testuser");

        Card card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setBalance(new BigDecimal("1000.00"));
        card.setStatus(Status.ACTIVE);

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);

        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.findAllByUser(user, pageable)).thenReturn(cardPage);
        when(modelMapper.map(card, CardDTO.class)).thenReturn(cardDTO);

        Page<CardDTO> result = cardService.getCards(user, null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().getFirst().getId());
    }

    @Test
    void getCards_ShouldReturnFilteredCards_WhenSearchProvided() {
        User user = new User();
        Card card = new Card();
        card.setId(1L);

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);

        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.findAllByUserAndCardNumberContainingIgnoreCase(user, "1234", pageable))
                .thenReturn(cardPage);
        when(modelMapper.map(card, CardDTO.class)).thenReturn(cardDTO);

        Page<CardDTO> result = cardService.getCards(user, "1234", pageable);

        assertEquals(1, result.getContent().size());
        verify(cardRepository).findAllByUserAndCardNumberContainingIgnoreCase(user, "1234", pageable);
    }

    @Test
    void getCard_ShouldReturnCard_WhenCardExists() {
        Card card = new Card();
        card.setId(1L);

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(modelMapper.map(card, CardDTO.class)).thenReturn(cardDTO);

        CardDTO result = cardService.getCard(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getCard_ShouldThrowException_WhenCardNotFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCard(1L));
    }

    @Test
    void blockCard_Admin_ShouldBlockCard_WhenCardIsPendingToBlocking() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(Status.PENDING_TO_BLOCKING);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.blockCard(1L);

        assertEquals(Status.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_Admin_ShouldThrowException_WhenCardNotPendingToBlocking() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(Status.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardNotReadyToBlockedException.class, () -> cardService.blockCard(1L));
    }

    @Test
    void blockCard_ShouldBlockCard_WhenCardExists() {
        User user = new User();
        Card card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setStatus(Status.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.blockCard(1L, user);

        assertEquals(Status.PENDING_TO_BLOCKING, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_ShouldThrowException_WhenCardNotBelongsToUser() {
        User user = new User();
        User anotherUser = new User();

        Card card = new Card();
        card.setId(1L);
        card.setUser(anotherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(1L, user));
    }

    @Test
    void activateCard_ShouldActivateCard_WhenCardExists() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(Status.PENDING);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.activateCard(1L);

        assertEquals(Status.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        Card card = new Card();
        card.setId(1L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card);
    }

    @Test
    void transfer_ShouldTransferMoney_WhenValidTransfer() {
        User user = new User();
        user.setUsername("testuser");

        Card senderCard = new Card();
        senderCard.setId(1L);
        senderCard.setUser(user);
        senderCard.setBalance(new BigDecimal("1000.00"));
        senderCard.setStatus(Status.ACTIVE);

        Card recipientCard = new Card();
        recipientCard.setId(2L);
        recipientCard.setUser(user);
        recipientCard.setBalance(new BigDecimal("500.00"));
        recipientCard.setStatus(Status.ACTIVE);
        recipientCard.setCardNumber("1234567890123456");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("1234567890123456")).thenReturn(Optional.of(recipientCard));

        cardService.transfer(1L, "1234567890123456", 100.0, user);

        assertEquals(new BigDecimal("900.00"), senderCard.getBalance());
        assertEquals(new BigDecimal("600.00"), recipientCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowException_WhenInsufficientBalance() {
        User user = new User();
        user.setUsername("testuser");

        Card senderCard = new Card();
        senderCard.setId(1L);
        senderCard.setUser(user);
        senderCard.setBalance(new BigDecimal("50.00"));
        senderCard.setStatus(Status.ACTIVE);

        Card recipientCard = new Card();
        recipientCard.setId(2L);
        recipientCard.setUser(user);
        recipientCard.setBalance(new BigDecimal("500.00"));
        recipientCard.setStatus(Status.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("1234567890123456")).thenReturn(Optional.of(recipientCard));

        assertThrows(InsufficientBalanceException.class,
                () -> cardService.transfer(1L, "1234567890123456", 100.0, user));
    }

    @Test
    void transfer_ShouldThrowException_WhenNegativeAmount() {
        User user = new User();
        Card senderCard = new Card();
        senderCard.setUser(user);
        senderCard.setBalance(new BigDecimal("1000.00"));

        Card recipientCard = new Card();
        recipientCard.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("1234567890123456")).thenReturn(Optional.of(recipientCard));

        assertThrows(InsufficientBalanceException.class,
                () -> cardService.transfer(1L, "1234567890123456", -100.0, user));
    }

    @Test
    void transfer_ShouldThrowException_WhenCardsNotActive() {
        User user = new User();
        Card senderCard = new Card();
        senderCard.setUser(user);
        senderCard.setBalance(new BigDecimal("1000.00"));
        senderCard.setStatus(Status.BLOCKED);

        Card recipientCard = new Card();
        recipientCard.setUser(user);
        recipientCard.setStatus(Status.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber("1234567890123456")).thenReturn(Optional.of(recipientCard));

        assertThrows(CardNotActivatedException.class,
                () -> cardService.transfer(1L, "1234567890123456", 100.0, user));
    }

    @Test
    void getBalance_ShouldReturnBalance_WhenCardBelongsToUser() {
        User user = new User();
        Card card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setBalance(new BigDecimal("1000.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        BigDecimal balance = cardService.getBalance(1L, user);

        assertEquals(new BigDecimal("1000.00"), balance);
    }

    @Test
    void getBalance_ShouldThrowException_WhenCardNotBelongsToUser() {
        User user = new User();
        user.setUsername("testuser");

        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");

        Card card = new Card();
        card.setId(1L);
        card.setUser(anotherUser);
        card.setBalance(new BigDecimal("1000.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardNotFoundException.class, () -> cardService.getBalance(1L, user));
    }

    @Test
    void updateExpirationTime_ShouldExpireCards_WhenCardsExpired() {
        Card expiredCard = new Card();
        expiredCard.setId(1L);
        expiredCard.setStatus(Status.ACTIVE);
        expiredCard.setValidityPeriod(LocalDateTime.now().minusDays(1));

        when(cardRepository.findByValidityPeriodBeforeAndStatusNot(any(LocalDateTime.class), eq(Status.EXPIRED)))
                .thenReturn(List.of(expiredCard));

        cardService.updateExpirationTime();

        assertEquals(Status.EXPIRED, expiredCard.getStatus());
        verify(cardRepository).saveAll(List.of(expiredCard));
    }
}