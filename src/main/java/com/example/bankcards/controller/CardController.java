package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public CardController(CardService cardService, UserService userService, ModelMapper modelMapper) {
        this.cardService = cardService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createCard(@RequestBody Card card, Principal principal) {
        cardService.createCard(card, userService.findByUsername(principal.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
