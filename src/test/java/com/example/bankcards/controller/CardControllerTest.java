package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CardController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        })
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void getCards_ShouldReturnPagedCards() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setRole(Role.USER);

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);
        cardDTO.setMaskedCardNumber("**** **** **** 1234");
        cardDTO.setBalance(new BigDecimal("1500.00"));

        Page<CardDTO> cardPage = new PageImpl<>(List.of(cardDTO));

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(cardService.getCards(eq(user), isNull(), any(Pageable.class))).thenReturn(cardPage);

        mockMvc.perform(get("/api/v1/cards")
                        .principal(() -> "testuser")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.content[0].balance").value(1500.0));
    }

    @Test
    @WithMockUser
    void getCards_ShouldReturnPagedCardsWithSearch() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);
        cardDTO.setMaskedCardNumber("**** **** **** 1234");
        cardDTO.setBalance(new BigDecimal("1000.00"));

        Page<CardDTO> cardPage = new PageImpl<>(List.of(cardDTO));

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(cardService.getCards(eq(user), eq("1234"), any(Pageable.class))).thenReturn(cardPage);

        mockMvc.perform(get("/api/v1/cards")
                        .principal(() -> "testuser")
                        .param("search", "1234")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 1234"));
    }

    @Test
    @WithMockUser
    void getBalance_ShouldReturnBalance() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(cardService.getBalance(1L, user)).thenReturn(new BigDecimal("1500.00"));

        mockMvc.perform(get("/api/v1/cards/1/balance")
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.0"));
    }

    @Test
    @WithMockUser
    void blockCard_ShouldReturnOk() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        when(userService.findByUsername("testuser")).thenReturn(user);

        mockMvc.perform(put("/api/v1/cards/1/block")
                        .principal(() -> "testuser"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void transfer_ShouldReturnOk() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        when(userService.findByUsername("testuser")).thenReturn(user);

        mockMvc.perform(put("/api/v1/cards/1/transfer/1234567890123456")
                        .principal(() -> "testuser")
                        .param("amount", "100.0"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getCards_ShouldReturnEmptyPage_WhenNoCards() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        Page<CardDTO> emptyPage = new PageImpl<>(List.of());

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(cardService.getCards(eq(user), isNull(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/cards")
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}