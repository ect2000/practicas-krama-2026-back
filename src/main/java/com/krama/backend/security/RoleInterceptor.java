package com.krama.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // ---> 1. PERMITIR SIEMPRE PETICIONES DE PRE-VUELO (CORS) <---
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Excepción para el Login
        if (path.contains("/login")) {
            return true;
        }

        // Protegemos las rutas críticas
        if (path.startsWith("/api/usuarios") || path.startsWith("/api/clientes") || path.startsWith("/api/proyectos")) {
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String rol = jwtUtil.extractAllClaims(token).get("rol", String.class);

                    if ("ADMIN".equals(rol)) {
                        return true; 
                    }
                } catch (Exception e) {
                    System.out.println("Error validando token en Interceptor: " + e.getMessage());
                }
            }
            
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Se requiere rol de Administrador");
            return false;
        }

        return true;
    }
}