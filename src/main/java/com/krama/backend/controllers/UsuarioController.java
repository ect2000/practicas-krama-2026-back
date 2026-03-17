package com.krama.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krama.backend.models.Usuario;
import com.krama.backend.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Esta función se ejecuta cuando alguien entra a la URL con el método GET
    @GetMapping
    public List<Usuario> obtenerTodosLosUsuarios() {
        // Usamos la magia del repositorio para buscar todos los usuarios en MariaDB
        return usuarioRepository.findAll();
    }
}