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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

    // Este endpoint nos permite buscar a un usuario específico por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> ResponseEntity.ok().body(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario nuevoUsuario) {
        
        // 1. Validamos si el email ya existe usando nuestro nuevo método del Repositorio
        if (usuarioRepository.existsByEmail(nuevoUsuario.getEmail())) {
            // Si existe, devolvemos un error HTTP 400 (Bad Request) con un mensaje claro
            return ResponseEntity.badRequest().body("Error: Ya existe una cuenta con el correo " + nuevoUsuario.getEmail());
        }

        // 2. Si no existe, guardamos el usuario en la base de datos como siempre
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        
        // 3. Intentamos enviar el correo electrónico
        try {
            emailService.enviarEmailBienvenida(usuarioGuardado.getEmail(), usuarioGuardado.getNombre());
            System.out.println("¡Correo de bienvenida enviado con éxito a " + usuarioGuardado.getEmail() + "!");
        } catch (Exception e) {
            System.err.println("Error al enviar el correo a " + usuarioGuardado.getEmail() + ": " + e.getMessage());
        }

        // 4. Devolvemos el usuario guardado al frontend con un código HTTP 200 (OK)
        return ResponseEntity.ok(usuarioGuardado);
    }

    @PutMapping("/{id}")
    public Usuario actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            // Actualizamos los datos básicos
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setApellidos(usuarioActualizado.getApellidos());
            usuario.setEmail(usuarioActualizado.getEmail());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            usuario.setRol(usuarioActualizado.getRol());
            
            // SOLUCIÓN CONTRASEÑA: Solo la cambiamos si nos llega una nueva y no está vacía
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
                usuario.setPassword(usuarioActualizado.getPassword());
            }

            // EXTRA: Debemos guardar las listas de clientes y proyectos que conseguiste enviar desde Angular
            if (usuarioActualizado.getClientes() != null) {
                usuario.setClientes(usuarioActualizado.getClientes());
            }
            if (usuarioActualizado.getProyectos() != null) {
                usuario.setProyectos(usuarioActualizado.getProyectos());
            }
            
            return usuarioRepository.save(usuario);
        }).orElse(null);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody Usuario credenciales) {
        
        // 1. Buscamos en la base de datos si existe alguien con el email que nos envían
        Usuario usuarioEncontrado = usuarioRepository.findByEmail(credenciales.getEmail());

        // 2. Comprobamos si el usuario existe Y si la contraseña coincide
        if (usuarioEncontrado != null && usuarioEncontrado.getPassword().equals(credenciales.getPassword())) {
            
            // ¡Éxito! El portero le deja pasar y devolvemos los datos del usuario
            return ResponseEntity.ok(usuarioEncontrado);
            
        } else {
            // Fracaso: o el email no existe, o la contraseña está mal. 
            // Devolvemos un error 401 (No Autorizado)
            return ResponseEntity.status(401).body("Error: Email o contraseña incorrectos");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().build(); // Devuelve 200 OK si lo logra
        } catch (DataIntegrityViolationException e) {
            // Si la base de datos lo bloquea, devolvemos un 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar el usuario porque tiene proyectos u horas asociadas.");
        }
    }
}