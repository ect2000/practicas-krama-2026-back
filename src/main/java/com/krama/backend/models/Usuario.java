package com.krama.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.ManyToOne;
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
    
    private String email;

    private String telefono;

    private String rol;
    
    // ¡Borramos el Long idCliente y ponemos esto!
    // @ManyToOne indica la relación (Muchos usuarios pueden pertenecer a un cliente)
    // @JoinColumn le dice a MariaDB cómo se llamará la columna exacta en la tabla
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

}