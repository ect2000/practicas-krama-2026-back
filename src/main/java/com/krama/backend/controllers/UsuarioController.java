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

    /**
     * Devuelve una lista con todos los usuarios del sistema.
     * @return Lista de todos los usuarios.
     */
    @GetMapping
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca y devuelve un usuario específico por su ID.
     * @param id Identificador del usuario.
     * @return ResponseEntity con el usuario encontrado o estado notFound.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> ResponseEntity.ok().body(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- NUEVO MÉTODO DE VALIDACIÓN MODIFICADO ---
    /**
     * Valida que los proyectos asignados al usuario pertenezcan a los clientes que tiene asignados.
     * Se omite esta comprobación para los usuarios con rol ADMIN.
     * @param usuario Usuario con los proyectos y clientes a validar.
     * @return Null si es válido, ResponseEntity con error en caso contrario.
     */
    private ResponseEntity<?> validarProyectosDeClientes(Usuario usuario) {
        
        // 1. EXCEPCIÓN DE SEGURIDAD: Los administradores gestionan todo, omitimos esta restricción
        if ("ADMIN".equals(usuario.getRol())) {
            return null; // Todo OK, pasa la validación automáticamente
        }

        List<Cliente> clientesAsignados = usuario.getClientes();
        List<Proyecto> proyectosAsignados = usuario.getProyectos();

        // Si no hay clientes o proyectos asignados, no hay nada que validar.
        if (clientesAsignados == null || clientesAsignados.isEmpty() || 
            proyectosAsignados == null || proyectosAsignados.isEmpty()) {
            return null; 
        }

        // Obtenemos una lista rápida con los IDs de los clientes seleccionados
        List<Long> idsClientes = clientesAsignados.stream()
                .map(Cliente::getId)
                .collect(Collectors.toList());

        // Verificamos cada proyecto que se intenta guardar
        for (Proyecto p : proyectosAsignados) {
            Proyecto proyectoReal = proyectoRepository.findById(p.getId()).orElse(null);

            if (proyectoReal != null && proyectoReal.getCliente() != null) {
                if (!idsClientes.contains(proyectoReal.getCliente().getId())) {
                    return ResponseEntity.badRequest().body("Inyección de datos detectada: El proyecto ID " + 
                        proyectoReal.getId() + " no pertenece a ninguno de los clientes asignados al usuario.");
                }
            }
        }
        
        return null; // Todo OK
    }

    /**
     * Registra un nuevo usuario encriptando su contraseña y enviando un email de bienvenida.
     * @param nuevoUsuario Datos del usuario a crear.
     * @return ResponseEntity con el usuario creado o un error si el correo ya existe.
     */
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
    /**
     * Actualiza los datos de un usuario existente validando sus relaciones.
     * @param id ID del usuario a modificar.
     * @param usuarioActualizado Datos actualizados.
     * @return ResponseEntity con el usuario guardado o un error de validación.
     */
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

    /**
     * Autentica al usuario mediante email y contraseña, devolviendo un token JWT.
     * @param credenciales Objeto con email y contraseña.
     * @return ResponseEntity con el usuario y el token de sesión o un error 401.
     */
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

    /**
     * Borra un usuario de la base de datos si no tiene restricciones de integridad.
     * @param id ID del usuario a eliminar.
     * @return ResponseEntity indicando éxito o conflicto si hay relaciones existentes.
     */
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