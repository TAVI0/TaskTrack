package com.tavio.lemon.web.controller;

import com.tavio.lemon.domain.service.UserService;
import com.tavio.lemon.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "Operaciones CRUD sobre usuarios")
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(summary = "Listar todos los usuarios",
            description = "Solo accesible por usuarios con rol ADMIN")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserEntity>> getAll() {
        List<UserEntity> users = userService.getAll();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Obtener un usuario por ID",
            description = "El propio usuario o ADMIN pueden acceder")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<UserEntity> getById(@PathVariable Long id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar usuario",
            description = "Actualiza username y/o password. Propio usuario o ADMIN")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<UserEntity> update(
            @PathVariable Long id,
            @RequestBody @Valid UserEntity reqBody) {
        return userService.getById(id).map(existing -> {
            existing.setUsername(reqBody.getUsername());
            if (reqBody.getPassword() != null && !reqBody.getPassword().isBlank()) {
                existing.setPassword(reqBody.getPassword());
            }
            UserEntity saved = userService.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar usuario",
            description = "El propio usuario o ADMIN pueden eliminar")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return userService.getById(id).map(user -> {
            userService.delete(id);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}