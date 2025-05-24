package com.tavio.lemon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavio.lemon.domain.service.AuthService;
import com.tavio.lemon.web.controller.AuthController;
import com.tavio.lemon.web.dto.AuthResponse;
import com.tavio.lemon.web.dto.LoginRequest;
import com.tavio.lemon.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;


    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }

    @Test
    @DisplayName("POST /auth/register → 200 + body")
    void register_Success() throws Exception {
        var req = new RegisterRequest("ana", "pass123");
        var resp = new AuthResponse("tok123", 5L, "ana", "USER");
        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("tok123"))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /auth/register → 403 si el usuario ya existe")
    void register_ElUsuarioYaExiste_Throws() throws Exception {
        var req = new RegisterRequest("ana", "pass123");
        when(authService.register(any()))
                .thenThrow(new IllegalArgumentException("El usuario ya existe"));

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /auth/login → 200 + body")
    void login_Success() throws Exception {
        var req = new LoginRequest("ana", "pass123");
        var resp = new AuthResponse("jwt456", 5L, "ana", "USER");
        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt456"))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /auth/login → 401 si credenciales incorrectas")
    void login_BadCredentials() throws Exception {
        var req = new LoginRequest("ana", "wrong");
        doThrow(new BadCredentialsException("Credenciales inválidas"))
                .when(authService).login(any());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isUnauthorized());
    }
}
