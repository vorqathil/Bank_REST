package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
    private final CardService cardService;
    private final UserService userService;

    public CardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping
    public Page<CardDTO> getCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String search,
            Principal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        return cardService.getCards(userService.findByUsername(principal.getName()), search, pageable);
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<?> block(@PathVariable Long cardId, Principal principal) {
        cardService.blockCard(cardId, userService.findByUsername(principal.getName()));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/transfer/{recipientCardNumber}")
    public ResponseEntity<?> transfer(@PathVariable Long cardId,
                                      @PathVariable String recipientCardNumber,
                                      @RequestParam("amount") double amount,
                                      Principal principal) {
        cardService.transfer(cardId, recipientCardNumber, amount, userService.findByUsername(principal.getName()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<Double> getBalance(@PathVariable Long cardId, Principal principal) {
        BigDecimal balance = cardService.getBalance(cardId, userService.findByUsername(principal.getName()));
        return ResponseEntity.ok(balance.doubleValue());
    }
}
