package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.Status;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureWebMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void createCard_ShouldReturnCreated_WhenValidRequest() throws Exception {
        doNothing().when(cardService).createCard(anyString(), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/admin/create")
                        .param("username", "testuser")
                        .param("balance", "1000.00"))
                .andExpect(status().isCreated());
    }

    @Test
    void getCards_ShouldReturnCardsList() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setRole(Role.USER);

        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);
        cardDTO.setMaskedCardNumber("**** **** **** 1234");
        cardDTO.setUser(userDTO);
        cardDTO.setStatus(Status.ACTIVE);
        cardDTO.setBalance(new BigDecimal("1000.00"));

        List<CardDTO> cards = List.of(cardDTO);
        when(cardService.getCards()).thenReturn(cards);

        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].maskedCardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$[0].user.username").value("testuser"));
    }

    @Test
    void blockCard_ShouldReturnOk() throws Exception {
        doNothing().when(cardService).blockCard(anyLong());

        mockMvc.perform(put("/api/v1/admin/1/block"))
                .andExpect(status().isOk());
    }

    @Test
    void activateCard_ShouldReturnOk() throws Exception {
        doNothing().when(cardService).activateCard(anyLong());

        mockMvc.perform(put("/api/v1/admin/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCard_ShouldReturnOk() throws Exception {
        doNothing().when(cardService).deleteCard(anyLong());

        mockMvc.perform(delete("/api/v1/admin/1"))
                .andExpect(status().isOk());
    }

    @Test
    void makeAdmin_ShouldReturnOk() throws Exception {
        doNothing().when(userService).makeAdmin(anyString());

        mockMvc.perform(put("/api/v1/admin/testuser/make-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void updateExpirationTime_ShouldReturnOk() throws Exception {
        doNothing().when(cardService).updateExpirationTime();

        mockMvc.perform(put("/api/v1/admin/update-expiration"))
                .andExpect(status().isOk());
    }
}