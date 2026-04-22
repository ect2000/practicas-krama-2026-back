package com.krama.backend.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

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

    // Seguridad: Evitamos que la contraseña se envíe al frontend por accidente
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String rol;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_cliente",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "cliente_id")
    )
    private List<Cliente> clientes;

    // --- CAMBIO CLAVE AQUÍ ---
    @ManyToMany(mappedBy = "usuarios", fetch = FetchType.EAGER)
    // Al listar los proyectos de un usuario, ignoramos "usuarios" (para no volver aquí)
    // e ignoramos "encargado" para evitar que se cargue de nuevo el perfil del administrador
    @JsonIgnoreProperties({"usuarios", "encargado"}) 
    private List<Proyecto> proyectos;
}