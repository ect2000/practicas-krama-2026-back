package com.krama.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krama.backend.models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Boot crea la consulta SQL automáticamente solo con leer este nombre
    boolean existsByEmail(String email);
}