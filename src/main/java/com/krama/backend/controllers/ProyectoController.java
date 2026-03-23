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

import com.krama.backend.models.Imputacion;
import com.krama.backend.models.Proyecto;
import com.krama.backend.repositories.ImputacionRepository;
import com.krama.backend.repositories.ProyectoRepository;

@RestController
@RequestMapping("/api/proyectos")
@CrossOrigin(origins = "http://localhost:8100")
public class ProyectoController {

    @Autowired
    private ProyectoRepository proyectoRepository;

    // AÑADIDO: Inyectamos el repositorio de imputaciones para poder buscar las horas gastadas
    @Autowired
    private ImputacionRepository imputacionRepository;

    @GetMapping
    public List<Proyecto> obtenerTodosLosProyectos() {
        return proyectoRepository.findAll();
    }

    // AÑADIDO: Nuevo endpoint para calcular la inteligencia del proyecto
    // La URL será: GET /api/proyectos/1/horas-restantes
    // La URL sigue siendo: GET /api/proyectos/{id}/horas-restantes
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

        // --- NUEVA INTELIGENCIA AQUÍ ---

        // Calculamos el porcentaje (con cuidado de no dividir entre cero si el proyecto no tiene horas presupuestadas)
        double porcentajeGastado = 0.0;
        if (horasPresupuestadas > 0) {
            porcentajeGastado = (horasGastadas / horasPresupuestadas) * 100;
        }

        // Decidimos el estado del proyecto basados en los números
        String estado = "Todo en orden";
        if (horasRestantes < 0) {
            estado = "¡Peligro! Horas superadas";
        } else if (porcentajeGastado >= 80.0) {
            // Si han gastado el 80% o más, lanzamos una advertencia
            estado = "Precaución: Presupuesto casi agotado"; 
        }

        // Cambiamos Double por Object para poder meter números y textos mezclados
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("horasPresupuestadas", horasPresupuestadas);
        resultado.put("horasGastadas", horasGastadas);
        resultado.put("horasRestantes", horasRestantes);
        
        // Añadimos nuestros nuevos datos inteligentes
        resultado.put("porcentajeGastado", Math.round(porcentajeGastado * 100.0) / 100.0); // Redondeamos a 2 decimales
        resultado.put("estado", estado);

        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public Proyecto crearProyecto(@RequestBody Proyecto nuevoProyecto) {
        return proyectoRepository.save(nuevoProyecto);
    }

    @PutMapping("/{id}")
    public Proyecto actualizarProyecto(@PathVariable Long id, @RequestBody Proyecto proyectoActualizado) {
        return proyectoRepository.findById(id).map(proyecto -> {
            proyecto.setNombre(proyectoActualizado.getNombre());
            proyecto.setCosteTotal(proyectoActualizado.getCosteTotal());
            proyecto.setHorasPresupuestadas(proyectoActualizado.getHorasPresupuestadas());
            proyecto.setCliente(proyectoActualizado.getCliente());
            proyecto.setUsuario(proyectoActualizado.getUsuario());
            return proyectoRepository.save(proyecto);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void borrarProyecto(@PathVariable Long id) {
        proyectoRepository.deleteById(id);
    }
}