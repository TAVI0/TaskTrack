package com.tavio.lemon.service;

import com.tavio.lemon.domain.repository.UserRepository;
import com.tavio.lemon.domain.service.UserService;
import com.tavio.lemon.entity.Role;
import com.tavio.lemon.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService service;

    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
                .id(7L)
                .username("pepito")
                .password("pwd")
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.parse("2025-05-22T00:00:00Z"))
                .updatedAt(Instant.parse("2025-05-22T00:00:00Z"))
                .build();
    }

    @Test
    void getByUsername_deberíaDelegarARepoYDevolverOptional() {
        when(repo.findByUsername("pepito")).thenReturn(Optional.of(sampleUser));

        Optional<UserEntity> opt = service.getByUsername("pepito");

        assertTrue(opt.isPresent());
        assertEquals(sampleUser, opt.get());
        verify(repo).findByUsername("pepito");
    }

    @Test
    void getByUsername_siNoExiste_retornaOptionalVacio() {
        when(repo.findByUsername("maria")).thenReturn(Optional.empty());

        Optional<UserEntity> opt = service.getByUsername("maria");

        assertFalse(opt.isPresent());
        verify(repo).findByUsername("maria");
    }

    @Test
    void getById_deberíaDelegarARepoYDevolverOptional() {
        when(repo.findById(7L)).thenReturn(Optional.of(sampleUser));

        Optional<UserEntity> opt = service.getById(7L);

        assertTrue(opt.isPresent());
        assertEquals(sampleUser, opt.get());
        verify(repo).findById(7L);
    }

    @Test
    void getById_siNoExiste_retornaOptionalVacio() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        Optional<UserEntity> opt = service.getById(99L);

        assertFalse(opt.isPresent());
        verify(repo).findById(99L);
    }

    @Test
    void getAll_deberíaDelegarARepoYDevolverLista() {
        List<UserEntity> lista = List.of(sampleUser);
        when(repo.findAll()).thenReturn(lista);

        List<UserEntity> resultado = service.getAll();

        assertSame(lista, resultado);
        verify(repo).findAll();
    }

    @Test
    void save_deberíaLlamarARepoSave_yDevolverEntidadGuardada() {
        when(repo.save(sampleUser)).thenReturn(sampleUser);

        UserEntity saved = service.save(sampleUser);

        assertNotNull(saved);
        assertEquals(sampleUser.getUsername(), saved.getUsername());
        verify(repo).save(sampleUser);
    }

    @Test
    void delete_deberíaLlamarARepoDeleteById() {
        Long id = 7L;
        doNothing().when(repo).deleteById(id);

        service.delete(id);

        verify(repo).deleteById(id);
    }
}
