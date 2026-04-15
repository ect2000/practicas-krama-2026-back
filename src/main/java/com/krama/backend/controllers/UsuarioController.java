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
import org.mindrot.jbcrypt.BCrypt;

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

    @Autowired
    private com.krama.backend.security.JwtUtil jwtUtil;

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
        
        if (usuarioRepository.existsByEmail(nuevoUsuario.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Ya existe una cuenta con el correo " + nuevoUsuario.getEmail());
        }

        // ---> Encriptamos la contraseña antes de guardarla <---
        if (nuevoUsuario.getPassword() != null && !nuevoUsuario.getPassword().isEmpty()) {
            String hash = BCrypt.hashpw(nuevoUsuario.getPassword(), BCrypt.gensalt());
            nuevoUsuario.setPassword(hash);
        }

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        
        try {
            emailService.enviarEmailBienvenida(usuarioGuardado.getEmail(), usuarioGuardado.getNombre());
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }

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
                String hash = BCrypt.hashpw(usuarioActualizado.getPassword(), BCrypt.gensalt());
                usuario.setPassword(hash);
            }

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
        Usuario usuarioEncontrado = usuarioRepository.findByEmail(credenciales.getEmail());

        // Usamos BCrypt.checkpw para comparar la contraseña plana con el Hash
        if (usuarioEncontrado != null && BCrypt.checkpw(credenciales.getPassword(), usuarioEncontrado.getPassword())) {
            
            // ---> NUEVO: ¡MAGIA! Creamos el token <---
            String tokenGenerado = jwtUtil.generarToken(usuarioEncontrado);
            
            // Devolvemos el usuario Y el token a Angular empaquetados en un Mapa
            java.util.Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("usuario", usuarioEncontrado);
            respuesta.put("token", tokenGenerado);
            
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(401).body("Error: Email o contraseña incorrectos");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().build(); 
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar el usuario porque tiene proyectos u horas asociadas.");
        }
    }
}