package com.krama.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.krama.backend.models.Imputacion;
import com.krama.backend.models.Proyecto;
import com.krama.backend.models.Usuario;
import com.krama.backend.models.Notificacion;
import com.krama.backend.repositories.ImputacionRepository;
import com.krama.backend.repositories.ProyectoRepository;
import com.krama.backend.repositories.UsuarioRepository;
import com.krama.backend.repositories.NotificacionRepository;

@RestController
@RequestMapping("/api/imputaciones")
@CrossOrigin(origins = "http://localhost:8100")
public class ImputacionController {

    @Autowired
    private ImputacionRepository imputacionRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    /**
     * Obtiene una lista de todas las imputaciones registradas.
     * @return Lista de imputaciones.
     */
    @GetMapping
    public List<Imputacion> obtenerTodasLasImputaciones() {
        return imputacionRepository.findAll();
    }

    /**
     * Obtiene las imputaciones correspondientes a un usuario específico.
     * @param usuarioId ID del usuario.
     * @return Lista de imputaciones de ese usuario.
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<Imputacion> obtenerImputacionesDeUsuario(@PathVariable Long usuarioId) {
        return imputacionRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Obtiene las imputaciones registradas para un proyecto específico.
     * @param proyectoId ID del proyecto.
     * @return Lista de imputaciones del proyecto.
     */
    @GetMapping("/proyecto/{proyectoId}")
    public List<Imputacion> obtenerImputacionesDeProyecto(@PathVariable Long proyectoId) {
        return imputacionRepository.findByProyectoId(proyectoId);
    }

    /**
     * Genera un informe de imputaciones filtradas por listas de usuarios y proyectos.
     * @param usuarios Lista de IDs de usuarios a incluir en el informe.
     * @param proyectos Lista de IDs de proyectos a incluir.
     * @return ResponseEntity con la lista de imputaciones filtradas o error si faltan parámetros.
     */
    @GetMapping("/informe1")
    public ResponseEntity<List<Imputacion>> obtenerInforme1(
            @RequestParam(required = false) List<Long> usuarios, 
            @RequestParam(required = false) List<Long> proyectos) {
        
        // Validamos que nos envíen al menos un usuario y un proyecto
        if (usuarios == null || usuarios.isEmpty() || proyectos == null || proyectos.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Llamamos a nuestro nuevo método del repositorio
        List<Imputacion> resultado = imputacionRepository.findByUsuarioIdInAndProyectoIdIn(usuarios, proyectos);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Genera un informe de las horas imputadas por un usuario en un rango de fechas.
     * @param usuarioId ID del usuario a consultar.
     * @param fechaInicio Fecha inicial del rango.
     * @param fechaFin Fecha final del rango.
     * @return ResponseEntity con las imputaciones encontradas o error de validación.
     */
    @GetMapping("/informe2")
    public ResponseEntity<List<Imputacion>> obtenerInforme2(
            @RequestParam Long usuarioId, 
            @RequestParam java.time.LocalDate fechaInicio, 
            @RequestParam java.time.LocalDate fechaFin) {
        
        // Validamos que nos envíen todos los datos
        if (usuarioId == null || fechaInicio == null || fechaFin == null) {
            return ResponseEntity.badRequest().build();
        }

        // Buscamos las imputaciones del usuario en ese rango de fechas
        List<Imputacion> resultado = imputacionRepository.findByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene un informe de las imputaciones relacionadas a un cliente.
     * @param clienteId ID del cliente.
     * @return ResponseEntity con la lista de imputaciones del cliente.
     */
    @GetMapping("/informe3")
    public ResponseEntity<List<Imputacion>> obtenerInforme3(@RequestParam Long clienteId) {
        
        // Validamos que nos llegue el ID del cliente
        if (clienteId == null) {
            return ResponseEntity.badRequest().build();
        }

        // Buscamos usando el método mágico del repositorio
        List<Imputacion> resultado = imputacionRepository.findByProyectoClienteId(clienteId);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Crea una nueva imputación de horas en un proyecto.
     * Realiza validaciones sobre el límite diario de horas y el presupuesto del proyecto.
     * También notifica al encargado del proyecto en caso de éxito.
     * @param nuevaImputacion Los datos de la nueva imputación a registrar.
     * @return ResponseEntity con la imputación guardada o mensaje de error.
     */
    @PostMapping
    public ResponseEntity<?> crearImputacion(@RequestBody Imputacion nuevaImputacion) {
        
        // 1. Validaciones básicas de existencia
        if (nuevaImputacion.getProyecto() == null || nuevaImputacion.getProyecto().getId() == null) {
            return ResponseEntity.badRequest().body("Error: Debes indicar un ID de Proyecto válido.");
        }
        if (nuevaImputacion.getUsuario() == null || nuevaImputacion.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Error: Debes indicar un ID de Usuario válido.");
        }

        Proyecto proyecto = proyectoRepository.findById(nuevaImputacion.getProyecto().getId()).orElse(null);
        if (proyecto == null) {
            return ResponseEntity.badRequest().body("Error: El Proyecto con ID " + nuevaImputacion.getProyecto().getId() + " NO existe.");
        }

        Usuario usuario = usuarioRepository.findById(nuevaImputacion.getUsuario().getId()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Error: El Usuario con ID " + nuevaImputacion.getUsuario().getId() + " NO existe.");
        }

        // 2. Validaciones de horas básicas
        if (nuevaImputacion.getHoras() == null || nuevaImputacion.getHoras() <= 0) {
            return ResponseEntity.badRequest().body("Error: Las horas imputadas deben ser un número mayor a 0.");
        }
        
        // ---> NUEVA LÓGICA DE VALIDACIÓN DE 24 HORAS DIARIAS <---
        try {
            // Obtenemos cuántas horas lleva ya el usuario en ese día en concreto
            Double horasYaImputadas = imputacionRepository.sumarHorasPorUsuarioYFecha(
                usuario.getId(), 
                nuevaImputacion.getFecha()
            );

            // Calculamos el total
            Double totalHorasDia = horasYaImputadas + nuevaImputacion.getHoras();

            // Comprobamos el límite
            if (totalHorasDia > 24.0) {
                // Si se pasa de 24, mandamos un error 400 (Bad Request) al frontend
                double horasRestantes = 24.0 - horasYaImputadas;
                return ResponseEntity.badRequest().body(
                    "Error: El límite diario es de 24 horas. Ya tienes " + horasYaImputadas + 
                    "h registradas en esta fecha. Solo puedes añadir " + horasRestantes + "h más."
                );
            }
        } catch (Exception e) {
            System.err.println("Error al validar las horas diarias: " + e.getMessage());
        }
        // ---> FIN DE LA NUEVA LÓGICA <---

        // 3. Validación de Presupuesto del Proyecto
        if (proyecto.getHorasPresupuestadas() != null) {
            List<Imputacion> imputacionesActuales = imputacionRepository.findByProyectoId(proyecto.getId());
            double horasGastadas = 0.0;
            for (Imputacion imp : imputacionesActuales) {
                if (imp.getHoras() != null) {
                    horasGastadas += imp.getHoras();
                }
            }
            if ((horasGastadas + nuevaImputacion.getHoras()) > proyecto.getHorasPresupuestadas()) {
                double horasDisponibles = proyecto.getHorasPresupuestadas() - horasGastadas;
                return ResponseEntity.badRequest().body("Error: No puedes imputar. Superarías el presupuesto. Horas disponibles: " + horasDisponibles);
            }
        }

        // 4. Guardamos la imputación
        Imputacion imputacionGuardada = imputacionRepository.save(nuevaImputacion);

        // 5. Lógica de Notificación al Encargado
        try {
            Usuario encargado = proyecto.getEncargado();

            if (encargado != null) {
                Notificacion aviso = new Notificacion();
                aviso.setTitulo("Nueva Imputación");
                
                String anotacion = (nuevaImputacion.getAnotaciones() != null && !nuevaImputacion.getAnotaciones().trim().isEmpty()) 
                                ? nuevaImputacion.getAnotaciones() 
                                : "Sin comentarios";
                
                aviso.setMensaje("El usuario " + usuario.getNombre() + " ha añadido " + nuevaImputacion.getHoras() + 
                                " h al proyecto '" + proyecto.getNombre() + "' con el comentario: " + anotacion);
                aviso.setColor("tertiary");
                aviso.setIcono("time-outline");
                
                // Asignamos el destinatario único
                aviso.setUsuarioDestino(encargado); 
                
                notificacionRepository.save(aviso);
            } else {
                System.out.println("Aviso: El proyecto '" + proyecto.getNombre() + "' no tiene un encargado asignado.");
            }
        } catch (Exception e) {
            System.err.println("Error al crear la notificación para el encargado: " + e.getMessage());
        }

        return ResponseEntity.ok(imputacionGuardada);
    }

    /**
     * Modifica los datos de una imputación existente.
     * @param id ID de la imputación a modificar.
     * @param imputacionActualizada Los nuevos datos de la imputación.
     * @return La imputación actualizada o null si no se encuentra.
     */
    @PutMapping("/{id}")
    public Imputacion actualizarImputacion(@PathVariable Long id, @RequestBody Imputacion imputacionActualizada) {
        return imputacionRepository.findById(id).map(imputacion -> {
            imputacion.setProyecto(imputacionActualizada.getProyecto());
            imputacion.setUsuario(imputacionActualizada.getUsuario());
            imputacion.setFecha(imputacionActualizada.getFecha());
            imputacion.setHoras(imputacionActualizada.getHoras());
            imputacion.setAnotaciones(imputacionActualizada.getAnotaciones());
            return imputacionRepository.save(imputacion);
        }).orElse(null);
    }

    /**
     * Borra una imputación del sistema usando su identificador.
     * @param id ID de la imputación a borrar.
     */
    @DeleteMapping("/{id}")
    public void borrarImputacion(@PathVariable Long id) {
        imputacionRepository.deleteById(id);
    }
}