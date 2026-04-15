package com.krama.backend.security;

import com.krama.backend.models.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    // Generamos una clave secreta súper segura de forma automática
    private static final Key CLAVE_SECRETA = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // El token durará 10 horas
    private static final long TIEMPO_EXPIRACION = 1000 * 60 * 60 * 10; 

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail()) // <-- CORREGIDO: getEmail() en lugar de getMail()
                .claim("id", usuario.getId())  
                .claim("rol", usuario.getRol()) 
                .setIssuedAt(new Date()) 
                .setExpiration(new Date(System.currentTimeMillis() + TIEMPO_EXPIRACION))
                .signWith(CLAVE_SECRETA) 
                .compact();
    }
}