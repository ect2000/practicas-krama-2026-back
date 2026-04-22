package com.krama.backend.security;

import com.krama.backend.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    // ---> CAMBIO: Usamos una clave fija en lugar de una aleatoria <---
    private static final String SECRET_STRING = "MiClaveSecretaKrama2026SuperSeguraYLarga123456";
    private static final Key CLAVE_SECRETA = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    
    private static final long TIEMPO_EXPIRACION = 1000 * 60 * 60 * 10;

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail()) 
                .claim("id", usuario.getId())  
                .claim("rol", usuario.getRol()) 
                .setIssuedAt(new Date()) 
                .setExpiration(new Date(System.currentTimeMillis() + TIEMPO_EXPIRACION))
                .signWith(CLAVE_SECRETA) 
                .compact();
    }

    // ---> MÉTODO NUEVO PARA LEER EL TOKEN <---
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(CLAVE_SECRETA)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}