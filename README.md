# Proyecto Krama - Backend API

## 1. Descripción General
Este repositorio contiene el código del servidor (Backend) de **Proyecto Krama**, una plataforma integral de gestión que permite la administración de clientes, proyectos, imputaciones (registro de tiempos/estimaciones) y usuarios. El servidor expone una API REST robusta y segura que gestiona toda la lógica de negocio y el acceso a la base de datos.

## 2. Tecnologías Utilizadas
- **Lenguaje**: Java
- **Framework**: Spring Boot
- **Gestor de dependencias**: Maven
- **Persistencia de datos**: Spring Data JPA (Repositories)
- **Seguridad**: JWT (JSON Web Tokens)

## 3. Estructura y Módulos
El servidor sigue el patrón MVC (Modelo-Vista-Controlador) y está estructurado en:

### Controladores (`controllers/`)
Exponen las rutas de la API REST para la comunicación con el frontend:
- `ClienteController.java`
- `ProyectoController.java`
- `ImputacionController.java`
- `UsuarioController.java`
- `NotificacionController.java`

### Modelos (`models/`) y Repositorios (`repositories/`)
Definen las entidades de la base de datos y sus operaciones de persistencia (CRUD):
`Cliente`, `Proyecto`, `Imputacion`, `Usuario`, `Notificacion`.

### Servicios y Seguridad (`services/` & `security/`)
- `EmailService.java`: Servicio dedicado al envío de correos electrónicos.
- `JwtUtil.java`: Clase encargada de generar, firmar y validar los tokens de sesión de los usuarios.
- `RoleInterceptor.java`: Middleware/Interceptor que evalúa el nivel de permisos del usuario en cada petición (ej. diferenciando entre Empleado y Administrador).
- `CorsConfig.java` / `WebConfig.java`: Configuración de reglas CORS para permitir que el frontend se comunique de forma segura con el servidor.

## 4. Funcionalidades Clave
- **Control de Acceso basado en Roles (RBAC)**: Autenticación segura por JWT y validación de permisos en los interceptores.
- **Ciclo de vida del Trabajo**: Gestión en base de datos de clientes, proyectos e imputaciones.
- **Sistema de Notificaciones**: Servicio backend capaz de enviar correos electrónicos reales.
