package com.tavio.lemon.controller;


import com.tavio.lemon.domain.service.TaskService;
import com.tavio.lemon.domain.service.UserService;
import com.tavio.lemon.entity.Role;
import com.tavio.lemon.entity.TaskEntity;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.security.UserDetailsImpl;
import com.tavio.lemon.web.controller.TaskController;
import com.tavio.lemon.web.dto.TaskDto;
import com.tavio.lemon.web.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskController controller;

    private Authentication auth;
    private UserEntity user;
    private TaskEntity task;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(7L)
                .username("pepito")
                .role(Role.USER)
                .build();

        UserDetailsImpl principal = UserDetailsImpl.build(UserEntity.builder()
                .id(7L)
                .username("pepito")
                .build());
        auth = new UsernamePasswordAuthenticationToken(principal, null);

        task = TaskEntity.builder()
                .id(42L)
                .title("Tarea X")
                .description("Descripción")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();
    }

    @Test
    void listTasks_deberíaDevolverDtosParaElUsuario() {
        when(taskService.getAllByUser(7L)).thenReturn(List.of(task));

        ResponseEntity<List<TaskDto>> resp = controller.listTasks(auth);

        assertEquals(200, resp.getStatusCodeValue());
        List<TaskDto> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("Tarea X", body.get(0).getTitle());
        verify(taskService).getAllByUser(7L);
    }

    @Test
    void getById_siEsDelUsuario_retornaDto() {
        when(taskService.getById(42L)).thenReturn(Optional.of(task));

        ResponseEntity<TaskDto> resp = controller.getById(42L, auth);

        assertEquals(200, resp.getStatusCodeValue());
        TaskDto dto = resp.getBody();
        assertNotNull(dto);
        assertEquals(42L, dto.getId());
        verify(taskService).getById(42L);
    }

    @Test
    void getById_siNoExiste_lanza404() {
        when(taskService.getById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getById(99L, auth)
        );
        assertEquals("404 NOT_FOUND \"Tarea no encontrada\"", ex.getMessage());
    }

    @Test
    void getById_siNoEsPropia_lanza403() {
        UserEntity otro = UserEntity.builder().id(8L).build();
        TaskEntity t2 = TaskEntity.builder().id(100L).user(otro).build();
        when(taskService.getById(100L)).thenReturn(Optional.of(t2));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getById(100L, auth)
        );
        assertEquals("403 FORBIDDEN \"No autorizado\"", ex.getMessage());
    }

    @Test
    void createTask_conUsuarioExistente_retornaDtoCreado() {
        TaskDto dtoIn = TaskDto.builder()
                .title("Nueva")
                .description("Desc")
                .dueDate(LocalDateTime.now().plusDays(2))
                .userId(7L)
                .build();
        when(userService.getById(7L)).thenReturn(Optional.of(user));

        TaskEntity toSave = TaskMapper.toEntity(dtoIn, user);
        TaskEntity saved = TaskEntity.builder()
                .id(55L)
                .title("Nueva")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        when(taskService.save(any(TaskEntity.class))).thenReturn(saved);

        ResponseEntity<TaskDto> resp = controller.createTask(dtoIn, auth);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(55L, resp.getBody().getId());
        verify(userService).getById(7L);
        verify(taskService).save(any());
    }

    @Test
    void updateTask_siTodoOk_retornaDtoActualizado() {
        TaskDto dtoIn = TaskDto.builder()
                .title("Modificada")
                .description("Desc mod")
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        when(taskService.getById(42L)).thenReturn(Optional.of(task));
        TaskEntity updated = TaskEntity.builder()
                .id(42L)
                .title("Modificada")
                .user(user)
                .createdAt(task.getCreatedAt())
                .build();
        when(taskService.save(any(TaskEntity.class))).thenReturn(updated);

        ResponseEntity<TaskDto> resp = controller.updateTask(42L, dtoIn, auth);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("Modificada", resp.getBody().getTitle());
        verify(taskService).getById(42L);
        verify(taskService).save(any());
    }

    @Test
    void deleteTask_siEsPropia_devuelve204() {
        when(taskService.getById(42L)).thenReturn(Optional.of(task));
        doNothing().when(taskService).delete(42L);

        ResponseEntity<Void> resp = controller.deleteTask(42L, auth);

        assertEquals(204, resp.getStatusCodeValue());
        verify(taskService).delete(42L);
    }

    @Test
    void deleteTask_siNoEsPropia_lanza403() {
        TaskEntity t2 = TaskEntity.builder().id(99L).user(UserEntity.builder().id(9L).build()).build();
        when(taskService.getById(99L)).thenReturn(Optional.of(t2));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.deleteTask(99L, auth)
        );
        assertEquals("403 FORBIDDEN \"No autorizado\"", ex.getMessage());
    }
}
