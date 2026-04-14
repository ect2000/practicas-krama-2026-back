package com.krama.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

    // Este Bean se ejecuta automáticamente al arrancar el servidor
    @Bean
    CommandLineRunner initData(ClienteRepository clienteRepo, UsuarioRepository usuarioRepo, ProyectoRepository proyectoRepo) {
        return args -> {
            // Comprobamos si no hay clientes para no duplicar datos cada vez que reinicias
            if (clienteRepo.count() == 0) {
                
                // 1. Creamos un Cliente de prueba
                Cliente cliente = new Cliente();
                cliente.setNombre("Cliente de Prueba");
                cliente.setDescripcion("Empresa de desarrollo de software");
                clienteRepo.save(cliente);

                // 2. Creamos un Usuario de prueba vinculado a ese cliente
                Usuario usuario = new Usuario();
                usuario.setNombre("Admin");
                usuario.setApellidos("Pruebas");
                usuario.setEmail("admin@krama.com"); // Corregido: usamos setEmail en vez de setMail
                usuario.setClientes(List.of(cliente));
                usuarioRepo.save(usuario);

                // 3. Creamos un Proyecto de prueba
                Proyecto proyecto = new Proyecto();
                proyecto.setNombre("Modernización Web Krama");
                proyecto.setHorasPresupuestadas(200.0);
                proyecto.setCosteTotal(10000.0);
                proyecto.setCliente(cliente);
                
                // Corregido: Pasamos el usuario dentro de una Lista, ya que es @ManyToMany
                proyecto.setUsuarios(java.util.List.of(usuario)); 
                
                proyectoRepo.save(proyecto);
            }
        };
    }
}