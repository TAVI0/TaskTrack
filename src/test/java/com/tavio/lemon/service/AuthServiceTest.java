package com.tavio.lemon.service;

import com.tavio.lemon.domain.repository.UserRepository;
import com.tavio.lemon.domain.service.AuthService;
import com.tavio.lemon.entity.Role;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.security.UserDetailsImpl;
import com.tavio.lemon.web.config.JwtUtil;
import com.tavio.lemon.web.dto.AuthResponse;
import com.tavio.lemon.web.dto.LoginRequest;
import com.tavio.lemon.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authManager;
    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    AuthService authService;

    private RegisterRequest regReq;
    private LoginRequest loginReq;

    @BeforeEach
    void setUp() {
        regReq = RegisterRequest.builder()
                .username("juan")
                .password("secret")
                .build();
        loginReq = LoginRequest.builder()
                .username("juan")
                .password("secret")
                .build();
    }

    @Test
    void register_usuarioNuevo_devuelveAuthResponseConToken() {
        when(userRepo.existsByUsername("juan")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashedSecret");
        when(userRepo.save(any())).thenReturn(
            UserEntity.builder()
                .id(10L)
                .username("juan")
                .password("hashedSecret")
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .build());
        when(jwtUtil.generateToken(any(UserDetailsImpl.class))).thenReturn("tokenXYZ");

        AuthResponse resp = authService.register(regReq);

        assertEquals(10L, resp.getUserId());
        assertEquals("juan", resp.getUsername());
        assertEquals("USER", resp.getRole());
        assertEquals("tokenXYZ", resp.getToken());
        verify(userRepo).existsByUsername("juan");
        verify(userRepo).save(argThat(u -> u.getPassword().equals("hashedSecret")));
    }

    @Test
    void register_usuarioExistente_lanzaIllegalArgumentException() {
        when(userRepo.existsByUsername("juan")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.register(regReq));
        verify(userRepo, never()).save(any());
    }

    @Test
    void login_credencialesValidas_devuelveAuthResponseConToken() {
        UserDetailsImpl principal = UserDetailsImpl.build(
            UserEntity.builder()
                .id(20L)
                .username("juan")
                .password("hashed")
                .role(Role.ADMIN)
                .enabled(true)
                .createdAt(Instant.now())
                .build());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(authManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtUtil.generateToken(principal)).thenReturn("jwt123");

        AuthResponse resp = authService.login(loginReq);

        assertEquals(20L, resp.getUserId());
        assertEquals("juan", resp.getUsername());
        assertEquals("ADMIN", resp.getRole());
        assertEquals("jwt123", resp.getToken());
    }

    @Test
    void login_credencialesInvalidas_lanzaBadCredentialsException() {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad"));
        assertThrows(BadCredentialsException.class, () -> authService.login(loginReq));
    }
}
