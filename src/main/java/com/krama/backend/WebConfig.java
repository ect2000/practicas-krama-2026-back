package com.krama.backend;

import com.krama.backend.security.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
}