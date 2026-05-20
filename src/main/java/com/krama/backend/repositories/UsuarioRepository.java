package com.krama.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.krama.backend.models.Usuario;
import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Comprueba si existe un usuario con el email especificado.
     * @param email El email a comprobar.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByEmail(String email);

    // Spring Boot crea la consulta SQL automáticamente solo con leer este nombre
    /**
     * Busca un usuario por su email.
     * @param email El email del usuario.
     * @return El usuario encontrado o null.
     */
    Usuario findByEmail(String email);

    /**
     * Busca los usuarios que tienen un rol específico.
     * @param rol El rol a buscar.
     * @return Una lista de usuarios con ese rol.
     */
    List<Usuario> findByRol(String rol);
}