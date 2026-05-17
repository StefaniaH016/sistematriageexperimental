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

            LOG.info("Verificando datos iniciales de demostración...");

            // Estudiante de prueba
            usuarioRepository.findByEmail("juan.perez@uq.edu.co").ifPresentOrElse(
                u -> {
                    u.setIdentificacion("1001234567");
                    u.setNombre("Juan");
                    u.setApellido("Pérez");
                    u.setRol(Rol.ESTUDIANTE);
                    u.setPassword(passwordEncoder.encode("123456"));
                    u.setActivo(true);
                    usuarioRepository.save(u);
                },
                () -> usuarioRepository.save(Usuario.builder()
                        .identificacion("1001234567")
                        .nombre("Juan")
                        .apellido("Pérez")
                        .email("juan.perez@uq.edu.co")
                        .rol(Rol.ESTUDIANTE)
                        .password(passwordEncoder.encode("123456"))
                        .activo(true)
                        .build())
            );

            // Estudiante de prueba 2
            usuarioRepository.findByEmail("maria.garcia@uq.edu.co").ifPresentOrElse(
                u -> {
                    u.setIdentificacion("1009876543");
                    u.setNombre("María");
                    u.setApellido("García");
                    u.setRol(Rol.ESTUDIANTE);
                    u.setPassword(passwordEncoder.encode("123456"));
                    u.setActivo(true);
                    usuarioRepository.save(u);
                },
                () -> usuarioRepository.save(Usuario.builder()
                        .identificacion("1009876543")
                        .nombre("María")
                        .apellido("García")
                        .email("maria.garcia@uq.edu.co")
                        .rol(Rol.ESTUDIANTE)
                        .password(passwordEncoder.encode("123456"))
                        .activo(true)
                        .build())
            );

            // Docente / Responsable 1 (Carlos López)
            usuarioRepository.findByEmail("carlos.lopez@uq.edu.co").ifPresentOrElse(
                u -> {
                    u.setIdentificacion("8001234567");
                    u.setNombre("Carlos");
                    u.setApellido("López");
                    u.setRol(Rol.RESPONSABLE);
                    u.setPassword(passwordEncoder.encode("123456"));
                    u.setActivo(true);
                    usuarioRepository.save(u);
                },
                () -> usuarioRepository.save(Usuario.builder()
                        .identificacion("8001234567")
                        .nombre("Carlos")
                        .apellido("López")
                        .email("carlos.lopez@uq.edu.co")
                        .rol(Rol.RESPONSABLE)
                        .password(passwordEncoder.encode("123456"))
                        .activo(true)
                        .build())
            );

            // Docente / Responsable 2 (Pedro Ramírez, para pruebas de DOCENTE_EMAIL)
            usuarioRepository.findByEmail("pedro.ramirez@uq.edu.co").ifPresentOrElse(
                u -> {
                    u.setIdentificacion("8001234568");
                    u.setNombre("Pedro");
                    u.setApellido("Ramírez");
                    u.setRol(Rol.RESPONSABLE);
                    u.setPassword(passwordEncoder.encode("123456"));
                    u.setActivo(true);
                    usuarioRepository.save(u);
                },
                () -> usuarioRepository.save(Usuario.builder()
                        .identificacion("8001234568")
                        .nombre("Pedro")
                        .apellido("Ramírez")
                        .email("pedro.ramirez@uq.edu.co")
                        .rol(Rol.RESPONSABLE)
                        .password(passwordEncoder.encode("123456"))
                        .activo(true)
                        .build())
            );

            // Administrativo
            usuarioRepository.findByEmail("ana.martinez@uq.edu.co").ifPresentOrElse(
                u -> {
                    u.setIdentificacion("9001234567");
                    u.setNombre("Ana");
                    u.setApellido("Martínez");
                    u.setRol(Rol.ADMINISTRATIVO);
                    u.setPassword(passwordEncoder.encode("admin123"));
                    u.setActivo(true);
                    usuarioRepository.save(u);
                },
                () -> usuarioRepository.save(Usuario.builder()
                        .identificacion("9001234567")
                        .nombre("Ana")
                        .apellido("Martínez")
                        .email("ana.martinez@uq.edu.co")
                        .rol(Rol.ADMINISTRATIVO)
                        .password(passwordEncoder.encode("admin123"))
                        .activo(true)
                        .build())
            );

            LOG.info("Verificación de datos iniciales completada.");
        };
    }
}
