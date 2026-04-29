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
        String method = request.getMethod(); // Obtenemos qué tipo de petición es (GET, POST, etc.)

        // ---> 1. PERMITIR SIEMPRE PETICIONES DE PRE-VUELO (CORS) <---
        if ("OPTIONS".equalsIgnoreCase(method)) {
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

                    // 1. Si es administrador, tiene acceso total a todo (GET, POST, PUT, DELETE)
                    if ("ADMIN".equals(rol)) {
                        return true; 
                    } 
                    
                    // 2. Si es usuario normal, solo le permitimos peticiones de lectura (GET)
                    // Esto permite llenar los selectores en imputaciones e informes sin dar permisos de edición
                    if ("USUARIO".equals(rol) && "GET".equalsIgnoreCase(method)) {
                        return true;
                    }

                    // 3. Si es un USUARIO intentando hacer POST/PUT/DELETE, le denegamos el acceso
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: No tienes permisos para modificar estos datos");
                    return false;
                    
                } catch (Exception e) {
                    System.out.println("Error validando token en Interceptor: " + e.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado. Por favor, inicie sesión nuevamente.");
                    return false;
                }
            }
            
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No se proporcionó un token de autenticación válido");
            return false;
        }

        return true;
    }
}