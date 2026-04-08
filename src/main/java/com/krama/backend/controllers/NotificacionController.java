package com.krama.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.krama.backend.models.Notificacion;
import com.krama.backend.repositories.NotificacionRepository;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "http://localhost:8100")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @GetMapping
    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }
}