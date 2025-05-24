package com.tavio.lemon.domain.service;


import com.tavio.lemon.domain.repository.UserRepository;
import com.tavio.lemon.entity.Role;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.security.UserDetailsImpl;
import com.tavio.lemon.web.config.JwtUtil;
import com.tavio.lemon.web.dto.AuthResponse;
import com.tavio.lemon.web.dto.LoginRequest;
import com.tavio.lemon.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authManager,
                       JwtUtil jwtUtil) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager     = authManager;
        this.jwtUtil         = jwtUtil;
    }

    /**
     * Registra un usuario nuevo.
     * @throws IllegalArgumentException si el username ya existe.
     */
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        UserEntity user = UserEntity.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        UserEntity saved = userRepository.save(user);

        UserDetailsImpl principal = UserDetailsImpl.build(saved);
        String token = jwtUtil.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .username(saved.getUsername())
                .role(saved.getRole().name())
                .build();
    }

    /**
     * Autentica credenciales y devuelve un JWT.
     * @throws BadCredentialsException si usuario/clave no coinciden.
     */
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUsername(),
                        req.getPassword()
                )
        );
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtUtil.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .userId(principal.getId())
                .username(principal.getUsername())
                .role(principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_",""))
                .build();
    }
}