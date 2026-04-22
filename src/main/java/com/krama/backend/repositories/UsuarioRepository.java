package com.krama.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.krama.backend.models.Usuario;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);
    // Spring Boot crea la consulta SQL automáticamente solo con leer este nombre
    Usuario findByEmail(String email);
    List<Usuario> findByRol(String rol);
}