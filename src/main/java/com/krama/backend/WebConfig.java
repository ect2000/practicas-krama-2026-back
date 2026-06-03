package com.krama.backend;

import com.krama.backend.security.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web general y de interceptores para la aplicación.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor);
    }

    /**
     * Filtro CORS global y de máxima prioridad.
     * Soluciona el error "Response to preflight request doesn't pass access control check"
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permite enviar credenciales (como tokens o cookies)
        config.setAllowCredentials(true); 
        
        // Permite conexiones desde cualquier origen (tu localhost, la app de Android, etc.)
        config.addAllowedOriginPattern("*"); 
        
        // Permite cualquier cabecera y cualquier método (GET, POST, OPTIONS, etc.)
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        // Aplica estas reglas a TODAS las rutas de tu API
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}