package com.krama.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para verificar los roles y permisos de los usuarios en las peticiones.
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Intercepta la petición antes de que llegue al controlador para verificar permisos de rol.
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @param handler El manejador seleccionado para la petición.
     * @return true si la petición es permitida, false si es denegada.
     * @throws Exception Si ocurre algún error procesando la petición.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod(); // Obtenemos qué tipo de petición es (GET, POST, etc.)

        // ---> 1. PERMITIR SIEMPRE PETICIONES DE PRE-VUELO (CORS) Y EL LOGIN <---
        if ("OPTIONS".equalsIgnoreCase(method) || path.contains("/login")) {
            return true;
        }

        // ---> 2. PROTEGER TODA LA API EN GENERAL <---
        if (path.startsWith("/api/")) {
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String rol = jwtUtil.extractAllClaims(token).get("rol", String.class);

                    // 2.1 Si es administrador, tiene acceso total a todo
                    if ("ADMIN".equals(rol)) {
                        return true; 
                    } 
                    
                    // 2.2 Si NO es administrador, protegemos la modificación de datos críticos
                    if (path.startsWith("/api/usuarios") || path.startsWith("/api/clientes") || path.startsWith("/api/proyectos")) {
                        // Si intenta hacer un POST, PUT o DELETE en rutas críticas, lo bloqueamos
                        if (!"GET".equalsIgnoreCase(method)) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Solo los administradores pueden modificar estos datos.");
                            return false;
                        }
                    }

                    // 2.3 Si es un GET (para cargar los desplegables) o va a otras rutas (como /api/imputaciones), le dejamos pasar
                    return true;
                    
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