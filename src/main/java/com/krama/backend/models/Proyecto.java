package com.krama.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "proyectos")
@Data
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // Usamos Double para los números con decimales (dinero/coste)
    @Column(name = "coste_total")
    private Double costeTotal;

    // Usamos Double o Integer para las horas. Pongamos Double por si hay "medias horas" (ej: 2.5 horas)
    @Column(name = "horas_presupuestadas")
    private Double horasPresupuestadas;

    // Claves foráneas temporales (las relacionaremos de verdad más adelante)
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

}