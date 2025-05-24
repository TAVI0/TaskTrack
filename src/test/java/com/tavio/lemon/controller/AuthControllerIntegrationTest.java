package com.tavio.lemon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavio.lemon.domain.repository.UserRepository;
import com.tavio.lemon.entity.Role;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.web.dto.AuthResponse;
import com.tavio.lemon.web.dto.LoginRequest;
import com.tavio.lemon.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepo;

    @BeforeEach
    void setup() {
        userRepo.deleteAll();
    }

    @Test
    void register_NewUser_Returns200AndSavesUser() throws Exception {
        var req = new RegisterRequest("juan", "secret123");

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.username").value("juan"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andDo(mvcResult -> {
                    var resp = mapper.readValue(mvcResult.getResponse().getContentAsString(), AuthResponse.class);
                    UserEntity u = userRepo.findById(resp.getUserId()).orElseThrow();
                    assertThat(u.getUsername()).isEqualTo("juan");
                    assertThat(u.isEnabled()).isTrue();
                });
    }

    @Test
    void register_DuplicateUser_Returns403() throws Exception {
        userRepo.save(UserEntity.builder()
                .username("maria")
                .password("irrelevant")
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()
        );

        var req = new RegisterRequest("maria", "otraClave");
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void login_ValidCredentials_Returns200AndToken() throws Exception {
        String raw = "pwd123";
        UserEntity u = UserEntity.builder()
                .username("pedro")
                .password( new BCryptPasswordEncoder().encode(raw) )
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepo.save(u);

        var req = new LoginRequest("pedro", raw);
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(u.getId()))
                .andExpect(jsonPath("$.username").value("pedro"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_InvalidCredentials_Returns401() throws Exception {
        String raw = "correct";
        userRepo.save(UserEntity.builder()
                .username("luis")
                .password(new BCryptPasswordEncoder().encode(raw))
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()
        );

        var req = new LoginRequest("luis", "wrongpass");
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                )
                .andExpect(status().isUnauthorized());
    }
}
