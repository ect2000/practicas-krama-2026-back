package com.krama.backend.controllers;

import java.util.List;
import java.util.stream.Collectors; // IMPORTANTE: Añadido para el manejo de listas

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
import com.krama.backend.models.Cliente;
import com.krama.backend.models.Proyecto;
import com.krama.backend.repositories.UsuarioRepository;
import com.krama.backend.repositories.ProyectoRepository; // IMPORTANTE: Añadido
import com.krama.backend.services.EmailService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:8100")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProyectoRepository proyectoRepository; // Inyectamos el repositorio de proyectos

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.krama.backend.security.JwtUtil jwtUtil;

    @GetMapping
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> ResponseEntity.ok().body(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- NUEVO MÉTODO DE VALIDACIÓN ---
    private ResponseEntity<?> validarProyectosDeClientes(Usuario usuario) {
        List<Cliente> clientesAsignados = usuario.getClientes();
        List<Proyecto> proyectosAsignados = usuario.getProyectos();

        // Si no hay clientes asignados, la regla dice que puede seleccionar entre todos.
        // Si no hay proyectos asignados, no hay nada que validar.
        if (clientesAsignados == null || clientesAsignados.isEmpty() || 
            proyectosAsignados == null || proyectosAsignados.isEmpty()) {
            return null; // Todo OK, pasa la validación
        }

        // Obtenemos una lista rápida con los IDs de los clientes seleccionados
        List<Long> idsClientes = clientesAsignados.stream()
                .map(Cliente::getId)
                .collect(Collectors.toList());

        // Verificamos cada proyecto que se intenta guardar
        for (Proyecto p : proyectosAsignados) {
            // Buscamos el proyecto real en la BBDD para evitar datos falseados del frontend
            Proyecto proyectoReal = proyectoRepository.findById(p.getId()).orElse(null);

            if (proyectoReal != null && proyectoReal.getCliente() != null) {
                // Si el proyecto tiene un cliente, ese cliente DEBE estar en la lista de clientes del usuario
                if (!idsClientes.contains(proyectoReal.getCliente().getId())) {
                    return ResponseEntity.badRequest().body("Inyección de datos detectada: El proyecto ID " + 
                        proyectoReal.getId() + " no pertenece a ninguno de los clientes asignados al usuario.");
                }
            }
        }
        
        return null; // Todo OK
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario nuevoUsuario) {
        
        if (usuarioRepository.existsByEmail(nuevoUsuario.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Ya existe una cuenta con el correo " + nuevoUsuario.getEmail());
        }

        // Ejecutamos la validación de seguridad de los proyectos
        ResponseEntity<?> errorValidacion = validarProyectosDeClientes(nuevoUsuario);
        if (errorValidacion != null) {
            return errorValidacion;
        }

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

    // OJO: Hemos cambiado a ResponseEntity<?> para poder devolver errores de validación
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        
        // Ejecutamos la validación de seguridad de los proyectos
        ResponseEntity<?> errorValidacion = validarProyectosDeClientes(usuarioActualizado);
        if (errorValidacion != null) {
            return errorValidacion;
        }

        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setApellidos(usuarioActualizado.getApellidos());
            usuario.setEmail(usuarioActualizado.getEmail());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            usuario.setRol(usuarioActualizado.getRol());
            
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
            
            Usuario guardado = usuarioRepository.save(usuario);
            return ResponseEntity.ok(guardado);
            
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody Usuario credenciales) {
        Usuario usuarioEncontrado = usuarioRepository.findByEmail(credenciales.getEmail());

        if (usuarioEncontrado != null && BCrypt.checkpw(credenciales.getPassword(), usuarioEncontrado.getPassword())) {
            String tokenGenerado = jwtUtil.generarToken(usuarioEncontrado);
            
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