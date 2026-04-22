package com.krama.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // ---> NUEVO: Endpoint para buscar notificaciones de un usuario específico <---
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        
        // Llamamos al método que creaste en NotificacionRepository
        List<Notificacion> misNotificaciones = notificacionRepository.findByUsuarioDestinoIdOrderByIdDesc(usuarioId);
        
        return ResponseEntity.ok(misNotificaciones);
    }
}