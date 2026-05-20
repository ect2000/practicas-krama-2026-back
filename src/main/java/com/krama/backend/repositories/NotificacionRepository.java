package com.krama.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.krama.backend.models.Notificacion;
import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad Notificacion.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Busca las notificaciones de un usuario de destino, ordenadas por ID de forma descendente.
     * @param usuarioDestinoId El ID del usuario destino.
     * @return Una lista de notificaciones ordenadas.
     */
    List<Notificacion> findByUsuarioDestinoIdOrderByIdDesc(Long usuarioDestinoId);
}