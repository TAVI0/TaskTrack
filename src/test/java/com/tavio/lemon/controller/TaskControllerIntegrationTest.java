package com.tavio.lemon.controller;

import com.tavio.lemon.entity.Role;
import com.tavio.lemon.entity.TaskEntity;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavio.lemon.domain.repository.TaskRepository;
import com.tavio.lemon.domain.repository.UserRepository;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TaskRepository taskRepo;

    private Authentication auth;
    private UserEntity user;

    @BeforeEach
    void setup() {
        taskRepo.deleteAll();
        userRepo.deleteAll();


        user = userRepo.save(UserEntity.builder()
                .username("pepito")
                .password("pwd")
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        UserDetailsImpl principal = UserDetailsImpl.build(user);
        auth = new UsernamePasswordAuthenticationToken(principal, null);
    }

    @Test
    void flujoCrearYListarTareas_integraCapas() throws Exception {
        String json = """
            {
              "title":"Integración",
              "description":"Test end-to-end",
              "createdAt":"%s",
              "dueDate":"%s",
              "userId":%d
            }
            """.formatted(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                user.getId()
        );

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .principal(auth)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Integración"));

        mvc.perform(get("/tasks")
                        .principal(auth)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Integración"));
    }

    @Test
    void getById_integraYDevuelve404SiNoExiste() throws Exception {
        mvc.perform(get("/tasks/{id}", 123)
                        .principal(auth)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_integraYDevuelve200SiExisteYEsPropia() throws Exception {
        TaskEntity saved = taskRepo.save(
                TaskEntity.builder()
                        .title("Test X")
                        .description("...")
                        .user(user)
                        .dueDate(LocalDateTime.now().plusDays(1))
                        .build()
        );

        mvc.perform(get("/tasks/{id}", saved.getId())
                        .principal(auth)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Test X"));
    }
}