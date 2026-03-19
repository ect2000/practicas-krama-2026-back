package com.krama.backend.controllers;

import java.util.List;

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
import com.krama.backend.repositories.ImputacionRepository;

@RestController
@RequestMapping("/api/imputaciones")
@CrossOrigin(origins = "http://localhost:8100")
public class ImputacionController {

    @Autowired
    private ImputacionRepository imputacionRepository;

    @GetMapping
    public List<Imputacion> obtenerTodasLasImputaciones() {
        return imputacionRepository.findAll();
    }

    // Ruta para obtener solo las imputaciones de un usuario concreto (ej. /api/imputaciones/usuario/1)
    @GetMapping("/usuario/{usuarioId}")
    public List<Imputacion> obtenerImputacionesDeUsuario(@PathVariable Long usuarioId) {
        return imputacionRepository.findByUsuarioId(usuarioId);
    }

    // Ruta para obtener las imputaciones de un proyecto (ej. /api/imputaciones/proyecto/2)
    @GetMapping("/proyecto/{proyectoId}")
    public List<Imputacion> obtenerImputacionesDeProyecto(@PathVariable Long proyectoId) {
        return imputacionRepository.findByProyectoId(proyectoId);
    }

    @PostMapping
    public ResponseEntity<?> crearImputacion(@RequestBody Imputacion nuevaImputacion) {
        
        // 1. Validar que las horas no vengan vacías y sean mayores a 0
        if (nuevaImputacion.getHoras() == null || nuevaImputacion.getHoras() <= 0) {
            return ResponseEntity.badRequest().body("Error: Las horas imputadas deben ser un número mayor a 0.");
        }

        // 2. Validar que las horas tengan sentido (nadie trabaja más de 24h al día)
        if (nuevaImputacion.getHoras() > 24) {
            return ResponseEntity.badRequest().body("Error: No puedes imputar más de 24 horas en un solo registro.");
        }

        // 3. Validar que la imputación esté asignada a quién y dónde corresponde
        if (nuevaImputacion.getProyecto() == null || nuevaImputacion.getUsuario() == null) {
            return ResponseEntity.badRequest().body("Error: Toda imputación debe tener asignado un proyecto y un usuario.");
        }

        // Si supera todas las pruebas, la guardamos en la base de datos
        Imputacion imputacionGuardada = imputacionRepository.save(nuevaImputacion);
        
        // Devolvemos la respuesta exitosa con los datos guardados
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