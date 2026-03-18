package com.krama.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
public class ImputacionController {

    @Autowired
    private ImputacionRepository imputacionRepository;

    @GetMapping
    public List<Imputacion> obtenerTodasLasImputaciones() {
        return imputacionRepository.findAll();
    }

    @PostMapping
    public Imputacion crearImputacion(@RequestBody Imputacion nuevaImputacion) {
        return imputacionRepository.save(nuevaImputacion);
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