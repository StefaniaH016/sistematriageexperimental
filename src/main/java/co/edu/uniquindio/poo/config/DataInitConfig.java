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

/**
 * Configuración para cargar datos iniciales de demostración en MariaDB.
 * Crea usuarios de prueba al iniciar la aplicación.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    @Bean
    CommandLineRunner initData(UsuarioRepository usuarioRepository, 
                              PasswordEncoder passwordEncoder, 
                              JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Actualizar usuarios con rol obsoleto DOCENTE a RESPONSABLE
                jdbcTemplate.execute("UPDATE usuarios SET rol = 'RESPONSABLE' WHERE rol = 'DOCENTE'");
                System.out.println("✓ Migración completada: DOCENTE -> RESPONSABLE");
            } catch (Exception e) {
                System.out.println("⚠ Migración DOCENTE no disponible: " + e.getMessage());
            }

            try {
                // Si hay una sola solicitud con ID mayor a 1, reasignarla al ID 1
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM solicitudes", Integer.class);
                if (count != null && count == 1) {
                    Long oldId = jdbcTemplate.queryForObject("SELECT id FROM solicitudes LIMIT 1", Long.class);
                    if (oldId != null && oldId > 1) {
                        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                        jdbcTemplate.execute("UPDATE solicitudes SET id = 1 WHERE id = " + oldId);
                        jdbcTemplate.execute("UPDATE historial_solicitudes SET solicitud_id = 1 WHERE solicitud_id = " + oldId);
                        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
                        System.out.println("✓ ID de solicitud restablecido a 1");
                    }
                }

                jdbcTemplate.execute("ALTER TABLE solicitudes AUTO_INCREMENT = 1");
                System.out.println("✓ Auto-incremento de solicitudes restablecido");
            } catch (Exception e) {
                System.out.println("⚠ No se pudo restablecer auto-incremento: " + e.getMessage());
            }

            System.out.println("Creando usuarios de prueba...");

            // Estudiante 1: Juan Pérez
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, 
                "juan.perez@uq.edu.co", "1001234567", "Juan", "Pérez", Rol.ESTUDIANTE, "123456");

            // Estudiante 2: María García
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, 
                "maria.garcia@uq.edu.co", "1009876543", "María", "García", Rol.ESTUDIANTE, "123456");

            // Responsable 1: Carlos López
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, 
                "carlos.lopez@uq.edu.co", "8001234567", "Carlos", "López", Rol.RESPONSABLE, "123456");

            // Responsable 2: Pedro Ramírez
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, 
                "pedro.ramirez@uq.edu.co", "8001234568", "Pedro", "Ramírez", Rol.RESPONSABLE, "123456");

            // Administrativo: Ana Martínez
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, 
                "ana.martinez@uq.edu.co", "9001234567", "Ana", "Martínez", Rol.ADMINISTRATIVO, "admin123");

            System.out.println("✓ Usuarios de prueba listos");
        };
    }

    /**
     * Crea o actualiza un usuario existente con los datos proporcionados.
     */
    @SuppressWarnings("null")
    private void crearOActualizarUsuario(UsuarioRepository repo, 
                                         PasswordEncoder encoder,
                                         String email, 
                                         String identificacion, 
                                         String nombre, 
                                         String apellido, 
                                         Rol rol, 
                                         String password) {
        var usuarioOpt = repo.findByEmail(email);
        
        if (usuarioOpt.isPresent()) {
            // Usuario existe: actualizar
            Usuario usuario = usuarioOpt.get();
            usuario.setIdentificacion(identificacion);
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setRol(rol);
            usuario.setPassword(encoder.encode(password));
            usuario.setActivo(true);
            repo.save(usuario);
        } else {
            // Usuario no existe: crear
            Usuario usuario = Usuario.builder()
                    .email(email)
                    .identificacion(identificacion)
                    .nombre(nombre)
                    .apellido(apellido)
                    .rol(rol)
                    .password(encoder.encode(password))
                    .activo(true)
                    .build();
            repo.save(usuario);
        }
    }
}
