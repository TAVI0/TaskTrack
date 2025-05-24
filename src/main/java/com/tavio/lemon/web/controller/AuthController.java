package com.tavio.lemon.web.controller;


import com.tavio.lemon.domain.service.AuthService;
import com.tavio.lemon.web.dto.AuthResponse;
import com.tavio.lemon.web.dto.LoginRequest;
import com.tavio.lemon.web.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Endpoints para registro y login de usuarios")
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @Operation(
            summary     = "Registrar un nuevo usuario",
            description = "Crea una cuenta de usuario y devuelve un JWT para autorizar posteriores peticiones."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "403", description = "Datos inválidos en el body (violación de validaciones)"),
    })
    @PostMapping(
            path        = "/register",
            consumes    = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Validated RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary     = "Login de usuario",
            description = "Valida credenciales y devuelve un JWT válido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve token"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping(
            path        = "/login",
            consumes    = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Validated LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }



    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCreds(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}