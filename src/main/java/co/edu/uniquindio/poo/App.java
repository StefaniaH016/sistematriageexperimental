package co.edu.uniquindio.poo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del Sistema de Triage y Gestión de Solicitudes Académicas.
 * 
 * Programa de Ingeniería de Sistemas y Computación - Universidad del Quindío.
 * 
 * Este sistema permite:
 * - Registrar y clasificar solicitudes académicas (RF-01, RF-02)
 * - Priorizar solicitudes mediante reglas de negocio (RF-03)
 * - Gestionar el ciclo de vida completo de las solicitudes (RF-04)
 * - Asignar responsables y mantener historial auditable (RF-05, RF-06)
 * - Consultar y filtrar solicitudes por múltiples criterios (RF-07)
 * - Cerrar solicitudes con validaciones de integridad (RF-08)
 * - Funcionar de manera independiente de servicios de IA (RF-11)
 * - Exponer servicios mediante API REST (RF-12)
 * - Controlar acceso según roles de usuario (RF-13)
 * 
 * @author Equipo de Desarrollo
 * @version 1.0
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        System.out.println("============================================================");
        System.out.println("  Sistema de Triage y Gestión de Solicitudes Académicas");
        System.out.println("  API REST disponible en: http://localhost:8080/api");
        System.out.println("  Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("  Base de Datos: MariaDB en puerto 3307");
        System.out.println("============================================================");
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner fixDbColumns(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE solicitudes MODIFY tipo_solicitud VARCHAR(100)");
                jdbcTemplate.execute("ALTER TABLE historial_solicitudes MODIFY accion VARCHAR(500)");
                System.out.println("✅ Columnas de base de datos ajustadas correctamente para evitar Data Truncation.");
            } catch (Exception e) {
                System.err.println("Advertencia al ajustar columnas: " + e.getMessage());
            }
        };
    }
}
