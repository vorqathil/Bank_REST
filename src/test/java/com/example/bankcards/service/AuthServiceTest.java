package com.example.bankcards.service;

import com.example.bankcards.dto.AuthenticationDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnToken_WhenValidUser() {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setUsername("testuser");
        authDTO.setPassword("testpass123");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass123");

        User savedUser = new User();
        savedUser.setUsername("testuser");
        savedUser.setRole(Role.USER);

        when(modelMapper.map(authDTO, User.class)).thenReturn(user);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("testpass123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken("testuser")).thenReturn("jwt-token");

        String result = authService.register(authDTO);

        assertEquals("jwt-token", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUserExists() {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setUsername("existinguser");
        authDTO.setPassword("testpass123");

        User user = new User();
        user.setUsername("existinguser");

        when(modelMapper.map(authDTO, User.class)).thenReturn(user);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(authDTO));
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setUsername("testuser");
        authDTO.setPassword("testpass123");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtUtil.generateAccessToken("testuser")).thenReturn("jwt-token");

        String result = authService.login(authDTO);

        assertEquals("jwt-token", result);
    }

    @Test
    void login_ShouldThrowException_WhenInvalidCredentials() {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setUsername("testuser");
        authDTO.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(authDTO));
    }
}