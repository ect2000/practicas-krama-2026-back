package com.krama.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.krama.backend.models.Usuario;
import com.krama.backend.repositories.UsuarioRepository;
import com.krama.backend.services.EmailService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:8100")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario nuevoUsuario) {
        // 1. Guardamos el usuario en la base de datos como siempre
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        
        // 2. Intentamos enviar el correo electrónico
        try {
            // Le pasamos el email y el nombre que nos acaban de enviar
            emailService.enviarEmailBienvenida(usuarioGuardado.getEmail(), usuarioGuardado.getNombre());
            System.out.println("¡Correo de bienvenida enviado con éxito a " + usuarioGuardado.getEmail() + "!");
        } catch (Exception e) {
            // Si el correo es inventado o hay un fallo de internet, evitamos que el programa explote
            System.err.println("Error al enviar el correo a " + usuarioGuardado.getEmail() + ": " + e.getMessage());
        }

        // 3. Devolvemos el usuario guardado al frontend
        return usuarioGuardado;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            // Actualizamos todos los campos del usuario
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setApellidos(usuarioActualizado.getApellidos());
            usuario.setEmail(usuarioActualizado.getEmail());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            usuario.setRol(usuarioActualizado.getRol());
            usuario.setCliente(usuarioActualizado.getCliente());
            
            // Guardamos y devolvemos un 200 OK con los datos guardados
            return ResponseEntity.ok(usuarioRepository.save(usuario));
            
        }).orElse(ResponseEntity.notFound().build()); // Si no lo encuentra, devuelve error 404
    }

    @DeleteMapping("/{id}")
    public void borrarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
    }
}