package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final CardService cardService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public AdminController(CardService cardService, UserService userService, ModelMapper modelMapper) {
        this.cardService = cardService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createCard(@RequestParam("username") String username,
                                           @RequestParam("balance") BigDecimal balance) {
        cardService.createCard(username, balance);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping()
    public List<Card> getCards() {
        return cardService.getCards();
    }

    @GetMapping("/{cardId}")
    public Card getCard(@PathVariable("cardId") Long cardId) {
        return cardService.getCard(cardId);
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<Void> blockCard(@PathVariable("cardId") Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable("cardId") Long cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable("cardId") Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/make-admin")
    public ResponseEntity<Void> makeAdmin(@PathVariable("username") String username) {
        userService.makeAdmin(username);
        return ResponseEntity.ok().build();
    }
}