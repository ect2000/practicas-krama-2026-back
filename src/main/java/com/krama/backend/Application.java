package com.krama.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.mindrot.jbcrypt.BCrypt;

import com.krama.backend.models.Cliente;
import com.krama.backend.models.Proyecto;
import com.krama.backend.models.Usuario;
import com.krama.backend.repositories.ClienteRepository;
import com.krama.backend.repositories.ProyectoRepository;
import com.krama.backend.repositories.UsuarioRepository;
import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner initData(ClienteRepository clienteRepo, UsuarioRepository usuarioRepo, ProyectoRepository proyectoRepo) {
        return args -> {
            
            // Obtenemos todos los usuarios actuales
            List<Usuario> usuarios = usuarioRepo.findAll();
            
            // Si la base de datos está vacía, creamos los datos iniciales
            if (usuarios.isEmpty()) {
                Cliente cliente = new Cliente();
                cliente.setNombre("Cliente de Prueba");
                cliente.setDescripcion("Empresa de desarrollo de software");
                clienteRepo.save(cliente);

                Usuario usuario = new Usuario();
                usuario.setNombre("Admin");
                usuario.setApellidos("Pruebas");
                usuario.setEmail("admin@krama.com"); 
                usuario.setClientes(List.of(cliente));
                
                String hashPassword = BCrypt.hashpw("123456", BCrypt.gensalt());
                usuario.setPassword(hashPassword);
                usuario.setRol("ADMIN"); 

                usuarioRepo.save(usuario);

                Proyecto proyecto = new Proyecto();
                proyecto.setNombre("Modernización Web Krama");
                proyecto.setHorasPresupuestadas(200.0);
                proyecto.setCosteTotal(10000.0);
                proyecto.setCliente(cliente);
                proyecto.setUsuarios(java.util.List.of(usuario)); 
                
                proyectoRepo.save(proyecto);
                System.out.println("✅ ÉXITO: Datos iniciales creados.");
                
            } else {
                
                // Si los datos ya existen, forzamos la actualización de la contraseña
                for (Usuario u : usuarios) {
                    if ("admin@krama.com".equals(u.getEmail())) {
                        String hashPassword = BCrypt.hashpw("123456", BCrypt.gensalt());
                        u.setPassword(hashPassword);
                        usuarioRepo.save(u);
                        System.out.println("✅ ÉXITO: Contraseña de administrador actualizada y cifrada.");
                    }
                }
            }
        };
    }
}