package com.tavio.lemon.web.controller;

import com.tavio.lemon.domain.service.TaskService;
import com.tavio.lemon.domain.service.UserService;
import com.tavio.lemon.entity.TaskEntity;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.security.UserDetailsImpl;
import com.tavio.lemon.web.dto.TaskDto;
import com.tavio.lemon.web.mapper.TaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Tag(name = "Tareas", description = "CRUD de tareas para el usuario autenticado")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    @Autowired
    public TaskController(TaskService taskService,
                          UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @Operation(summary = "Listar todas las tareas del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<TaskDto>> listTasks(Authentication auth) {
        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        List<TaskDto> dtos = taskService.getAllByUser(userId).stream()
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Obtener una tarea por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado para ver esta tarea"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getById(@PathVariable Long id,
                                           Authentication auth) {
        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        TaskEntity task = taskService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tarea no encontrada"));
        if (!task.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "No autorizado");
        }
        return ResponseEntity.ok(TaskMapper.toDto(task));
    }

    @Operation(summary = "Crear una nueva tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody @Valid TaskDto dto,
                                              Authentication auth) {
        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        UserEntity user = userService.getById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
        TaskEntity saved = taskService.save(TaskMapper.toEntity(dto, user));
        return ResponseEntity.ok(TaskMapper.toDto(saved));
    }

    @Operation(summary = "Actualizar por completo una tarea existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea actualizada correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado para actualizar esta tarea"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id,
                                              @RequestBody @Valid TaskDto dto,
                                              Authentication auth) {
        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        TaskEntity existing = taskService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tarea no encontrada"));
        if (!existing.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "No autorizado");
        }
        dto.setId(id);
        TaskEntity toSave = TaskMapper.toEntity(dto, existing.getUser());
        toSave.setCreatedAt(existing.getCreatedAt());
        TaskEntity saved = taskService.save(toSave);
        return ResponseEntity.ok(TaskMapper.toDto(saved));
    }

    @Operation(summary = "Eliminar una tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarea eliminada correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado para eliminar esta tarea"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                           Authentication auth) {
        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        TaskEntity task = taskService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tarea no encontrada"));
        if (!task.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "No autorizado");
        }
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}