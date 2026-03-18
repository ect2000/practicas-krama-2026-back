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

import com.krama.backend.models.Proyecto;
import com.krama.backend.repositories.ProyectoRepository;

@RestController
@RequestMapping("/api/proyectos")
@CrossOrigin(origins = "http://localhost:8100")
public class ProyectoController {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @GetMapping
    public List<Proyecto> obtenerTodosLosProyectos() {
        return proyectoRepository.findAll();
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