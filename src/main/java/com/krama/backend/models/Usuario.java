package com.krama.backend.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String apellidos;
    
    @Column(unique = true)
    private String email;

    private String telefono;

    private String password;

    private String rol;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    // --- CAMBIO IMPORTANTE AQUÍ ---
    // mappedBy indica que la configuración principal de esta relación la tiene la clase Proyecto en su variable "usuarios"
    @ManyToMany(mappedBy = "usuarios", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("usuarios")
    private List<Proyecto> proyectos;

}