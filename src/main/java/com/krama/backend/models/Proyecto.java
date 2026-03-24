package com.krama.backend.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

@Entity
@Table(name = "proyectos")
@Data
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(name = "coste_total")
    private Double costeTotal;

    @Column(name = "horas_presupuestadas")
    private Double horasPresupuestadas;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    // --- CAMBIO IMPORTANTE AQUÍ ---
    // Cambiamos @ManyToOne por @ManyToMany
    @ManyToMany
    @JoinTable(
        name = "proyectos_usuarios", // Nombre de la nueva tabla intermedia que creará MariaDB
        joinColumns = @JoinColumn(name = "id_proyecto"),
        inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    // @JsonIgnoreProperties evita un bug muy común donde el JSON se vuelve infinito al leer la base de datos
    @JsonIgnoreProperties("proyectos") 
    private List<Usuario> usuarios;

}