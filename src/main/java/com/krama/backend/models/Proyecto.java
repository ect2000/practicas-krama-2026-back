package com.krama.backend.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "proyectos")
@Data
/**
 * Clase que representa un proyecto en el sistema.
 */
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

    // ---> CAMBIO AQUÍ: Evitamos que el encargado intente cargar sus proyectos de nuevo <---
    @ManyToOne
    @JoinColumn(name = "id_encargado")
    @JsonIgnoreProperties({"proyectos", "clientes", "password"}) 
    private Usuario encargado;

    @ManyToMany
    @JoinTable(
        name = "proyectos_usuarios",
        joinColumns = @JoinColumn(name = "id_proyecto"),
        inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    // Esto ya lo tenías bien para los usuarios que trabajan en el proyecto
    @JsonIgnoreProperties("proyectos") 
    private List<Usuario> usuarios;
}