package co.edu.uniquindio.poo.config;

import co.edu.uniquindio.poo.model.entity.Usuario;
import co.edu.uniquindio.poo.model.enums.Rol;
import co.edu.uniquindio.poo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.logging.Logger;

/**
 * Configuración para cargar datos iniciales de demostración.
 * Crea usuarios de prueba al iniciar la aplicación (solo con H2 en memoria).
 */
@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    private static final Logger LOG = Logger.getLogger(DataInitConfig.class.getName());

    @Bean
    CommandLineRunner initData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Limpieza de base de datos para roles obsoletos (migración)
                jdbcTemplate.execute("UPDATE usuarios SET rol = 'RESPONSABLE' WHERE rol = 'DOCENTE'");
                LOG.info("Migración: Usuarios con rol DOCENTE actualizados a RESPONSABLE.");
            } catch (Exception e) {
                LOG.warning("No se pudo ejecutar migración de DOCENTE: " + e.getMessage());
            }

            if (usuarioRepository.count() == 0) {
                LOG.info("Cargando datos iniciales de demostración...");

                // Estudiante de prueba
                usuarioRepository.save(Usuario.builder()
                        .identificacion("1001234567")
                        .nombre("Juan")
                        .apellido("Pérez")
                        .email("juan.perez@uq.edu.co")
                        .rol(Rol.ESTUDIANTE)
                        .password(passwordEncoder.encode("123456"))
                        .activo(true)
                        .build());

                // Estudiante de prueba 2
                usuarioRepository.save(Usuario.builder()
                        .identificacion("1009876543")
                        .nombre("María")
                        .apellido("García")
                        .email("maria.garcia@uq.edu.co")
                        .rol(Rol.ESTUDIANTE)
                        .password(passwordEncoder.encode("123456"))
                        .activo(true)
                        .build());

                // Docente / Responsable
                usuarioRepository.save(Usuario.builder()
                        .identificacion("8001234567")
                        .nombre("Carlos")
                        .apellido("López")
                        .email("carlos.lopez@uq.edu.co")
                        .rol(Rol.RESPONSABLE)
                        .password(passwordEncoder.encode("123456"))
                        .activo(true)
                        .build());

                // Administrativo
                usuarioRepository.save(Usuario.builder()
                        .identificacion("9001234567")
                        .nombre("Ana")
                        .apellido("Martínez")
                        .email("ana.martinez@uq.edu.co")
                        .rol(Rol.ADMINISTRATIVO)
                        .password(passwordEncoder.encode("admin123"))
                        .activo(true)
                        .build());

                LOG.info("Datos iniciales cargados: 4 usuarios de prueba creados.");
            }
        };
    }
}
