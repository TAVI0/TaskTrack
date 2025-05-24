package com.tavio.lemon.web.config;

import com.tavio.lemon.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Clave secreta; en producción cargala de config externa (env var)
    private final Key secretKey = Keys.hmacShaKeyFor(
            "MiSuperSecretoParaJWTDeAlMenos32Caracteres!".getBytes()
    );

    // Tiempo de validez en ms (ej. 24h)
    private final long jwtExpirationMs = 24 * 60 * 60 * 1000;

    /**
     * Genera el token JWT usando username como subject.
     */
    public String generateToken(UserDetailsImpl userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el username del token.
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Valida firma y expiración.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // loguear error si querés
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}