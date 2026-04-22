package com.krama.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krama.backend.models.Cliente;
import com.krama.backend.models.Imputacion;
import com.krama.backend.models.Proyecto;
import com.krama.backend.models.Usuario;
import com.krama.backend.repositories.ClienteRepository;
import com.krama.backend.repositories.ImputacionRepository;
import com.krama.backend.repositories.ProyectoRepository;
import com.krama.backend.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api/proyectos")
@CrossOrigin(origins = "http://localhost:8100")
public class ProyectoController {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private ImputacionRepository imputacionRepository;

    // AÑADIDO: Inyectamos los repositorios para poder buscar antes de guardar
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping
    public List<Proyecto> obtenerTodosLosProyectos() {
        return proyectoRepository.findAll();
    }

    @GetMapping("/{id}/horas-restantes")
    public ResponseEntity<?> calcularHorasRestantes(@PathVariable Long id) {
        
        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);
        if (proyecto == null) {
            return ResponseEntity.notFound().build();
        }

        List<Imputacion> imputaciones = imputacionRepository.findByProyectoId(id);

        double horasGastadas = 0.0;
        for (Imputacion imp : imputaciones) {
            if (imp.getHoras() != null) {
                horasGastadas += imp.getHoras();
            }
        }

        double horasPresupuestadas = proyecto.getHorasPresupuestadas() != null ? proyecto.getHorasPresupuestadas() : 0.0;
        double horasRestantes = horasPresupuestadas - horasGastadas;

        double porcentajeGastado = 0.0;
        if (horasPresupuestadas > 0) {
            porcentajeGastado = (horasGastadas / horasPresupuestadas) * 100;
        }

        String estado = "Todo en orden";
        if (horasRestantes < 0) {
            estado = "¡Peligro! Horas superadas";
        } else if (porcentajeGastado >= 80.0) {
            estado = "Precaución: Presupuesto casi agotado"; 
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("horasPresupuestadas", horasPresupuestadas);
        resultado.put("horasGastadas", horasGastadas);
        resultado.put("horasRestantes", horasRestantes);
        resultado.put("porcentajeGastado", Math.round(porcentajeGastado * 100.0) / 100.0);
        resultado.put("estado", estado);

        return ResponseEntity.ok(resultado);
    }

    // --- CÓDIGO ACTUALIZADO A PRUEBA DE FALLOS ---
    @PostMapping
    public ResponseEntity<?> crearProyecto(@RequestBody Proyecto nuevoProyecto) {
        
        // 1. Asegurarnos de que el Cliente existe en la base de datos
        if (nuevoProyecto.getCliente() != null && nuevoProyecto.getCliente().getId() != null) {
            Cliente clienteReal = clienteRepository.findById(nuevoProyecto.getCliente().getId()).orElse(null);
            if (clienteReal == null) {
                return ResponseEntity.badRequest().body("Error: El cliente proporcionado no existe.");
            }
            nuevoProyecto.setCliente(clienteReal); // Le asignamos el cliente real completo
        }

        // 2. Asegurarnos de que los Usuarios existen en la base de datos
        if (nuevoProyecto.getUsuarios() != null && !nuevoProyecto.getUsuarios().isEmpty()) {
            List<Long> idsUsuarios = nuevoProyecto.getUsuarios().stream()
                                                  .map(Usuario::getId)
                                                  .toList();
            List<Usuario> usuariosReales = usuarioRepository.findAllById(idsUsuarios);
            nuevoProyecto.setUsuarios(usuariosReales); // Le asignamos los usuarios reales completos
        }

        // 3. Ahora que todo es correcto, guardamos el proyecto
        Proyecto proyectoGuardado = proyectoRepository.save(nuevoProyecto);
        return ResponseEntity.ok(proyectoGuardado);
    }

    // Archivo: src/main/java/com/krama/backend/controllers/ProyectoController.java

    @PutMapping("/{id}")
    public Proyecto actualizarProyecto(@PathVariable Long id, @RequestBody Proyecto proyectoActualizado) {
        return proyectoRepository.findById(id).map(proyecto -> {
            proyecto.setNombre(proyectoActualizado.getNombre());
            proyecto.setCosteTotal(proyectoActualizado.getCosteTotal());
            proyecto.setHorasPresupuestadas(proyectoActualizado.getHorasPresupuestadas());
            proyecto.setCliente(proyectoActualizado.getCliente());
            proyecto.setUsuarios(proyectoActualizado.getUsuarios());
            
            // ---> AÑADE ESTA LÍNEA PARA GUARDAR EL ENCARGADO <---
            proyecto.setEncargado(proyectoActualizado.getEncargado());
            
            return proyectoRepository.save(proyecto);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void borrarProyecto(@PathVariable Long id) {
        proyectoRepository.deleteById(id);
    }
}