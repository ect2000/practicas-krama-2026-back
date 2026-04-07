package com.krama.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.krama.backend.models.Imputacion;
import com.krama.backend.models.Proyecto;
import com.krama.backend.models.Usuario;
import com.krama.backend.repositories.ImputacionRepository;
import com.krama.backend.repositories.ProyectoRepository;
import com.krama.backend.repositories.UsuarioRepository; // ¡Importante nuevo repositorio!

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

    @GetMapping
    public List<Imputacion> obtenerTodasLasImputaciones() {
        return imputacionRepository.findAll();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Imputacion> obtenerImputacionesDeUsuario(@PathVariable Long usuarioId) {
        return imputacionRepository.findByUsuarioId(usuarioId);
    }

    @GetMapping("/proyecto/{proyectoId}")
    public List<Imputacion> obtenerImputacionesDeProyecto(@PathVariable Long proyectoId) {
        return imputacionRepository.findByProyectoId(proyectoId);
    }

    @PostMapping
    public ResponseEntity<?> crearImputacion(@RequestBody Imputacion nuevaImputacion) {
        
        // 1. Validar que vengan objetos con ID válido
        if (nuevaImputacion.getProyecto() == null || nuevaImputacion.getProyecto().getId() == null) {
            return ResponseEntity.badRequest().body("Error: Debes indicar un ID de Proyecto válido.");
        }
        if (nuevaImputacion.getUsuario() == null || nuevaImputacion.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Error: Debes indicar un ID de Usuario válido.");
        }

        // 2. Comprobar que el Proyecto realmente existe en la Base de Datos
        Proyecto proyecto = proyectoRepository.findById(nuevaImputacion.getProyecto().getId()).orElse(null);
        if (proyecto == null) {
            return ResponseEntity.badRequest().body("Error: El Proyecto con ID " + nuevaImputacion.getProyecto().getId() + " NO existe.");
        }

        // 3. Comprobar que el Usuario realmente existe en la Base de Datos
        Usuario usuario = usuarioRepository.findById(nuevaImputacion.getUsuario().getId()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Error: El Usuario con ID " + nuevaImputacion.getUsuario().getId() + " NO existe.");
        }

        // 4. Validar las horas
        if (nuevaImputacion.getHoras() == null || nuevaImputacion.getHoras() <= 0) {
            return ResponseEntity.badRequest().body("Error: Las horas imputadas deben ser un número mayor a 0.");
        }
        if (nuevaImputacion.getHoras() > 24) {
            return ResponseEntity.badRequest().body("Error: No puedes imputar más de 24 horas en un solo registro.");
        }

        // 5. Validar Presupuesto
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

        // 6. Si todo es correcto, guardamos
        Imputacion imputacionGuardada = imputacionRepository.save(nuevaImputacion);
        return ResponseEntity.ok(imputacionGuardada);
    }

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

    @DeleteMapping("/{id}")
    public void borrarImputacion(@PathVariable Long id) {
        imputacionRepository.deleteById(id);
    }
}