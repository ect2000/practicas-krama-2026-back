# Usamos una imagen de Java 17 (la versión que configuramos en tu pom.xml)
FROM eclipse-temurin:17-jdk-alpine

# Definimos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos todos tus archivos al contenedor
COPY . .

# Damos permiso de ejecución al script de Maven
RUN chmod +x mvnw

# Compilamos el proyecto saltando los tests para ir más rápido
RUN ./mvnw clean package -DskipTests

# Exponemos el puerto 8080 que es el que usa Spring Boot por defecto
EXPOSE 8080

# Comando para arrancar la aplicación
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]