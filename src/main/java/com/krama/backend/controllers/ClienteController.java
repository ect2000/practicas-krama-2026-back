package com.krama.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "http://localhost:8100")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    // 1. LEER (GET) - Obtener todos los clientes
    /**
     * Obtiene una lista de todos los clientes registrados en el sistema.
     * @return Lista de objetos Cliente.
     */
    @GetMapping
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    // 2. CREAR (POST) - Guardar un cliente nuevo
    /**
     * Crea un nuevo cliente y lo guarda en la base de datos.
     * @param nuevoCliente Objeto Cliente a guardar.
     * @return El cliente guardado.
     */
    @PostMapping
    public Cliente crearCliente(@RequestBody Cliente nuevoCliente) {
        return clienteRepository.save(nuevoCliente);
    }

    // 3. ACTUALIZAR (PUT) - Editar un cliente existente
    /**
     * Actualiza la información de un cliente existente basado en su ID.
     * @param id Identificador del cliente.
     * @param clienteActualizado Datos actualizados del cliente.
     * @return El cliente actualizado o null si no se encuentra.
     */
    @PutMapping("/{id}")
    public Cliente actualizarCliente(@PathVariable Long id, @RequestBody Cliente clienteActualizado) {
        return clienteRepository.findById(id).map(cliente -> {
            
            // ---> AÑADIMOS ESTA LÍNEA <---
            cliente.setCodigo(clienteActualizado.getCodigo());
            
            cliente.setNombre(clienteActualizado.getNombre());
            cliente.setDescripcion(clienteActualizado.getDescripcion());
            return clienteRepository.save(cliente);
        }).orElse(null); 
    }

    // 4. BORRAR (DELETE) - Eliminar un cliente
    // La URL será algo como /api/clientes/1
    /**
     * Elimina un cliente del sistema utilizando su ID.
     * @param id Identificador del cliente a eliminar.
     */
    @DeleteMapping("/{id}")
    public void borrarCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
    }
}