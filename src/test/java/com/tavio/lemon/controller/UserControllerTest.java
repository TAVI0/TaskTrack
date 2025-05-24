package com.tavio.lemon.controller;

import com.tavio.lemon.domain.service.UserService;
import com.tavio.lemon.entity.Role;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.web.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private UserEntity existing;

    @BeforeEach
    void setUp() {
        existing = UserEntity.builder()
                .id(10L)
                .username("juan")
                .password("pwd")
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getAll_deberíaDevolverLista() {
        List<UserEntity> lista = List.of(existing);
        when(userService.getAll()).thenReturn(lista);

        ResponseEntity<List<UserEntity>> resp = controller.getAll();

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(lista, resp.getBody());
        verify(userService).getAll();
    }

    @Test
    void getById_siExiste_retorna200YUsuario() {
        when(userService.getById(10L)).thenReturn(Optional.of(existing));

        ResponseEntity<UserEntity> resp = controller.getById(10L);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(existing, resp.getBody());
        verify(userService).getById(10L);
    }

    @Test
    void getById_siNoExiste_retorna404() {
        when(userService.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<UserEntity> resp = controller.getById(99L);

        assertEquals(404, resp.getStatusCodeValue());
        assertNull(resp.getBody());
        verify(userService).getById(99L);
    }

    @Test
    void update_siExiste_retorna200YActualiza() {
        UserEntity updatePayload = UserEntity.builder()
                .username("juan2")
                .password("newpwd")
                .build();
        UserEntity saved = UserEntity.builder()
                .id(10L)
                .username("juan2")
                .password("newpwd")
                .role(existing.getRole())
                .enabled(existing.isEnabled())
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(userService.getById(10L)).thenReturn(Optional.of(existing));
        when(userService.save(any(UserEntity.class))).thenReturn(saved);

        ResponseEntity<UserEntity> resp = controller.update(10L, updatePayload);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("juan2", resp.getBody().getUsername());
        assertEquals("newpwd", resp.getBody().getPassword());
        verify(userService).getById(10L);
        verify(userService).save(argThat(u ->
                u.getId().equals(10L) &&
                        u.getUsername().equals("juan2") &&
                        u.getPassword().equals("newpwd")
        ));
    }

    @Test
    void update_siNoExiste_retorna404() {
        UserEntity updatePayload = UserEntity.builder()
                .username("x")
                .build();
        when(userService.getById(20L)).thenReturn(Optional.empty());

        ResponseEntity<UserEntity> resp = controller.update(20L, updatePayload);

        assertEquals(404, resp.getStatusCodeValue());
        assertNull(resp.getBody());
        verify(userService).getById(20L);
        verify(userService, never()).save(any());
    }

    @Test
    void delete_siExiste_retorna204YElimina() {
        when(userService.getById(10L)).thenReturn(Optional.of(existing));
        // no need to stub delete

        ResponseEntity<Void> resp = controller.delete(10L);

        assertEquals(204, resp.getStatusCodeValue());
        verify(userService).getById(10L);
        verify(userService).delete(10L);
    }

    @Test
    void delete_siNoExiste_retorna404() {
        when(userService.getById(30L)).thenReturn(Optional.empty());

        ResponseEntity<Void> resp = controller.delete(30L);

        assertEquals(404, resp.getStatusCodeValue());
        verify(userService).getById(30L);
        verify(userService, never()).delete(anyLong());
    }
}