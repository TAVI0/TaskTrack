package com.tavio.lemon.service;


import com.tavio.lemon.domain.repository.TaskRepository;
import com.tavio.lemon.domain.service.TaskService;
import com.tavio.lemon.entity.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repo;

    @InjectMocks
    private TaskService service;

    private TaskEntity sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = TaskEntity.builder()
                .id(1L)
                .title("Prueba")
                .description("Descripción de prueba")
                .completed(false)
                .createdAt(LocalDateTime.of(2025, 5, 22, 0, 0))
                .dueDate(LocalDateTime.of(2025, 5, 23, 12, 0))
                .build();
    }

    @Test
    void getAllByUser_deberíaDelegarARepoYDevolverLista() {
        Long userId = 42L;
        List<TaskEntity> lista = List.of(sampleTask);
        when(repo.findByUserId(userId)).thenReturn(lista);

        List<TaskEntity> resultado = service.getAllByUser(userId);

        assertSame(lista, resultado);
        verify(repo, times(1)).findByUserId(userId);
    }

    @Test
    void getById_cuandoExiste_retornaOptionalConEntidad() {
        Long id = 1L;
        when(repo.findById(id)).thenReturn(Optional.of(sampleTask));

        Optional<TaskEntity> opt = service.getById(id);

        assertTrue(opt.isPresent());
        assertEquals(sampleTask, opt.get());
        verify(repo).findById(id);
    }

    @Test
    void getById_cuandoNoExiste_retornaOptionalVacio() {
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        Optional<TaskEntity> opt = service.getById(id);

        assertFalse(opt.isPresent());
        verify(repo).findById(id);
    }

    @Test
    void save_deberíaLlamarARepoSave_yDevolverEntidadGuardada() {
        when(repo.save(sampleTask)).thenReturn(sampleTask);

        TaskEntity guardada = service.save(sampleTask);

        assertNotNull(guardada);
        assertEquals(sampleTask.getTitle(), guardada.getTitle());
        verify(repo).save(sampleTask);
    }

    @Test
    void delete_deberíaLlamarARepoDeleteById() {
        Long id = 1L;
        doNothing().when(repo).deleteById(id);

        service.delete(id);

        verify(repo).deleteById(id);
    }
}
