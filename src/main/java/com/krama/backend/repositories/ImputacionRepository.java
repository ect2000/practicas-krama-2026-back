package com.krama.backend.repositories;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.krama.backend.models.Imputacion;

/**
 * Repositorio de Spring Data JPA para la entidad Imputacion.
 */
@Repository
public interface ImputacionRepository extends JpaRepository<Imputacion, Long> {
    
    /**
     * Busca las imputaciones realizadas por un usuario específico.
     * @param usuarioId El ID del usuario.
     * @return Una lista de imputaciones de ese usuario.
     */
    List<Imputacion> findByUsuarioId(Long usuarioId);

    /**
     * Busca las imputaciones asociadas a un proyecto específico.
     * @param proyectoId El ID del proyecto.
     * @return Una lista de imputaciones para ese proyecto.
     */
    List<Imputacion> findByProyectoId(Long proyectoId);

    // ---> AÑADE ESTA LÍNEA PARA EL INFORME 1 <---
    /**
     * Busca las imputaciones filtradas por una lista de usuarios y proyectos.
     * @param usuarioIds Lista de IDs de usuarios.
     * @param proyectoIds Lista de IDs de proyectos.
     * @return Una lista de imputaciones coincidentes.
     */
    List<Imputacion> findByUsuarioIdInAndProyectoIdIn(List<Long> usuarioIds, List<Long> proyectoIds);

    /**
     * Busca imputaciones de un usuario dentro de un rango de fechas.
     * @param usuarioId El ID del usuario.
     * @param fechaInicio La fecha inicial del rango.
     * @param fechaFin La fecha final del rango.
     * @return Una lista de imputaciones que coinciden con los criterios.
     */
    List<Imputacion> findByUsuarioIdAndFechaBetween(Long usuarioId, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin);

    /**
     * Busca todas las imputaciones que pertenecen a los proyectos de un cliente específico.
     * @param clienteId El ID del cliente.
     * @return Una lista de imputaciones asociadas a los proyectos de ese cliente.
     */
    List<Imputacion> findByProyectoClienteId(Long clienteId);

    /**
     * Suma el total de horas imputadas por un usuario en una fecha concreta.
     * @param usuarioId El ID del usuario.
     * @param fecha La fecha a consultar.
     * @return El total de horas (o 0 si no hay imputaciones).
     */
    @Query("SELECT COALESCE(SUM(i.horas), 0) FROM Imputacion i WHERE i.usuario.id = :usuarioId AND i.fecha = :fecha")
    Double sumarHorasPorUsuarioYFecha(@Param("usuarioId") Long usuarioId, @Param("fecha") LocalDate fecha);
}