package com.krama.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping; // Para borrar
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Para leer IDs de la URL
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping; // Para actualizar
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krama.backend.models.Cliente;
import com.krama.backend.repositories.ClienteRepository;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    // 1. LEER (GET) - Obtener todos los clientes
    @GetMapping
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    // 2. CREAR (POST) - Guardar un cliente nuevo
    @PostMapping
    public Cliente crearCliente(@RequestBody Cliente nuevoCliente) {
        return clienteRepository.save(nuevoCliente);
    }

    // 3. ACTUALIZAR (PUT) - Editar un cliente existente
    // La URL será algo como /api/clientes/1 (donde 1 es el ID)
    @PutMapping("/{id}")
    public Cliente actualizarCliente(@PathVariable Long id, @RequestBody Cliente clienteActualizado) {
        // Primero buscamos si el cliente existe. Si existe, lo actualizamos.
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setNombre(clienteActualizado.getNombre());
            cliente.setDescripcion(clienteActualizado.getDescripcion());
            return clienteRepository.save(cliente);
        }).orElse(null); // Si no existe, devolvemos null por ahora
    }

    // 4. BORRAR (DELETE) - Eliminar un cliente
    // La URL será algo como /api/clientes/1
    @DeleteMapping("/{id}")
    public void borrarCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
    }
}