package com.krama.backend; // Asegúrate de que esta primera línea esté aquí

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite el acceso a todas las rutas de tu API
                .allowedOrigins("http://localhost:8100") // Indica el puerto de tu Ionic
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Los métodos permitidos
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}