package com.krama.backend;

import com.krama.backend.security.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web general y de interceptores para la aplicación.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RoleInterceptor roleInterceptor;

    /**
     * Añade interceptores al registro de Spring MVC.
     * @param registry Registro de interceptores donde se añade el RoleInterceptor.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Aquí registramos nuestro nuevo interceptor
        registry.addInterceptor(roleInterceptor);
    }
    // Archivo: CorsConfig.java
    /**
     * Configura las reglas de CORS para permitir peticiones desde el frontend.
     * @param registry Registro de CORS a configurar.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Añadimos http://localhost (Android) y capacitor://localhost (iOS/Android)
                .allowedOrigins("http://localhost:8100", "http://localhost", "capacitor://localhost")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}