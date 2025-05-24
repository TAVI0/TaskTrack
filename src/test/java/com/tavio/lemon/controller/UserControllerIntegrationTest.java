package com.tavio.lemon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.entity.Role;
import com.tavio.lemon.security.UserDetailsImpl;
import com.tavio.lemon.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepo;

    private UserEntity user;
    private UserDetailsImpl principalUser;

    @BeforeEach
    void setup() {
        userRepo.deleteAll();

        user = userRepo.save(UserEntity.builder()
                .username("charlie")
                .password("pwd")
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        principalUser = UserDetailsImpl.build(user);
    }

    @Test
    void getAll_devolveráTodosLosUsuarios_comoAdmin() throws Exception {
        userRepo.save(UserEntity.builder()
                .username("alice").password("pwd")
                .role(Role.USER).enabled(true)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build());
        userRepo.save(UserEntity.builder()
                .username("bob").password("pwd")
                .role(Role.USER).enabled(true)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build());

        mvc.perform(get("/users")
                        .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].username",
                        containsInAnyOrder("charlie","alice","bob")));
    }

    @Test
    void getById_siExisteYEsPropio_retorna200YUsuario() throws Exception {
        mvc.perform(get("/users/{id}", user.getId())
                        .with(user(principalUser))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("charlie"));
    }

    @Test
    void getById_siNoExiste_comoAdmin_retorna404() throws Exception {
        mvc.perform(get("/users/{id}", 999L)
                        .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void update_siExisteYEsPropio_retorna200YActualiza() throws Exception {
        Map<String,String> payload = Map.of(
                "username","charlie2",
                "password","newpwd"
        );
        String json = mapper.writeValueAsString(payload);

        mvc.perform(put("/users/{id}", user.getId())
                        .with(user(principalUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("charlie2"));

        UserEntity updated = userRepo.findById(user.getId()).orElseThrow();
        assertThat(updated.getUsername()).isEqualTo("charlie2");
        assertThat(updated.getPassword()).isEqualTo("newpwd");
    }

    @Test
    void update_otherUser_retorna403() throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "username","validName",
                "password","validPwd"
        ));

        mvc.perform(put("/users/{id}", /*otro id distinto*/ 123L)
                        .with(user(principalUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void delete_siExisteYEsPropio_retorna204YElimina() throws Exception {
        mvc.perform(delete("/users/{id}", user.getId())
                        .with(user(principalUser))
                )
                .andExpect(status().isNoContent());

        assertThat(userRepo.findById(user.getId())).isEmpty();
    }

    @Test
    void delete_siNoExiste_retorna403() throws Exception {
        mvc.perform(delete("/users/{id}",777L)
                        .with(user(principalUser))
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void delete_siNoExiste_comoAdmin_retorna404() throws Exception {
        mvc.perform(delete("/users/{id}", 777L)
                        .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound());
    }
}
