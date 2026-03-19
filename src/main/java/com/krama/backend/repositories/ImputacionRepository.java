package com.krama.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krama.backend.models.Imputacion;

@Repository
public interface ImputacionRepository extends JpaRepository<Imputacion, Long> {
    
    // Spring Boot leerá este nombre y creará un "SELECT * FROM imputaciones WHERE usuario_id = ?"
    List<Imputacion> findByUsuarioId(Long usuarioId);

    // Búsqueda para ver todas las horas gastadas en un proyecto concreto
    List<Imputacion> findByProyectoId(Long proyectoId);
}