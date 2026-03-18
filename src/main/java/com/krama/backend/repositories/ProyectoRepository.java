package com.krama.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krama.backend.models.Proyecto;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
}