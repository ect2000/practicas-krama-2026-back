package com.krama.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krama.backend.models.Imputacion;

@Repository
public interface ImputacionRepository extends JpaRepository<Imputacion, Long> {
    
    List<Imputacion> findByUsuarioId(Long usuarioId);
    List<Imputacion> findByProyectoId(Long proyectoId);

    // ---> AÑADE ESTA LÍNEA PARA EL INFORME 1 <---
    List<Imputacion> findByUsuarioIdInAndProyectoIdIn(List<Long> usuarioIds, List<Long> proyectoIds);
}