package com.krama.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// @Service le dice a Spring Boot que esta clase es un "trabajador" que hace tareas de lógica de negocio
@Service
public class EmailService {

    // Esta es la herramienta mágica de Spring Boot que lee tu application.properties y se conecta a Gmail
    @Autowired
    private JavaMailSender mailSender;

    // Función que recibe a quién va el correo y cómo se llama la persona
    public void enviarEmailBienvenida(String destinatario, String nombreUsuario) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        
        mensaje.setTo(destinatario); // A quién se lo enviamos
        mensaje.setSubject("¡Bienvenido a Krama!"); // El asunto del correo
        
        // El cuerpo del mensaje (puedes personalizar este texto como quieras)
        mensaje.setText("Hola " + nombreUsuario + ",\n\n"
                + "Bienvenido a la plataforma de imputación horaria de Krama. "
                + "Tu cuenta ha sido creada con éxito y ya puedes empezar a registrar tus horas.\n\n"
                + "¡Un saludo del equipo de Krama!");

        // Damos la orden de enviar
        mailSender.send(mensaje);
    }
}