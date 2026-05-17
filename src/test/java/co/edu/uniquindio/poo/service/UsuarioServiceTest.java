package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.dto.usuario.UsuarioRequestDTO;
import co.edu.uniquindio.poo.dto.usuario.UsuarioResponseDTO;
import co.edu.uniquindio.poo.exception.OperacionNoPermitidaException;
import co.edu.uniquindio.poo.exception.ResourceNotFoundException;
import co.edu.uniquindio.poo.model.enums.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para el servicio de usuarios.
 * Verifica registro, validaciones de duplicados, activación/desactivación y consultas.
 */
@SpringBootTest
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    // ==================== REGISTRO ====================

    @Test
    @DisplayName("Registrar usuario nuevo exitosamente")
    void registrarUsuario_DebeRetornarUsuarioCreado() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .identificacion("9999999999")
                .nombre("Test")
                .apellido("Usuario")
                .email("test.usuario@uq.edu.co")
                .rol(Rol.ESTUDIANTE)
                .password("password123")
                .build();

        UsuarioResponseDTO resultado = usuarioService.registrarUsuario(request);

        assertNotNull(resultado.getId());
        assertEquals("9999999999", resultado.getIdentificacion());
        assertEquals("Test", resultado.getNombre());
        assertEquals("Usuario", resultado.getApellido());
        assertEquals("Test Usuario", resultado.getNombreCompleto());
        assertEquals(Rol.ESTUDIANTE, resultado.getRol());
        assertTrue(resultado.getActivo());
    }

    // ==================== VALIDACIÓN DE DUPLICADOS ====================

    @Test
    @DisplayName("Registrar usuario con identificación duplicada debe fallar")
    void registrarUsuarioConIdentificacionDuplicada_DebeFallar() {
        // La identificación "1001234567" ya existe (cargada por DataInitConfig)
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .identificacion("1001234567")
                .nombre("Otro")
                .apellido("Usuario")
                .email("otro.email@uq.edu.co")
                .rol(Rol.ESTUDIANTE)
                .password("password123")
                .build();

        assertThrows(OperacionNoPermitidaException.class,
                () -> usuarioService.registrarUsuario(request));
    }

    @Test
    @DisplayName("Registrar usuario con email duplicado debe fallar")
    void registrarUsuarioConEmailDuplicado_DebeFallar() {
        // El email "juan.perez@uq.edu.co" ya existe
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .identificacion("5555555555")
                .nombre("Nuevo")
                .apellido("Usuario")
                .email("juan.perez@uq.edu.co")
                .rol(Rol.ESTUDIANTE)
                .password("password123")
                .build();

        assertThrows(OperacionNoPermitidaException.class,
                () -> usuarioService.registrarUsuario(request));
    }

    // ==================== CONSULTAS ====================

    @Test
    @DisplayName("Obtener usuario por ID existente")
    void obtenerPorId_DebeRetornarUsuario() {
        UsuarioResponseDTO resultado = usuarioService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
    }

    @Test
    @DisplayName("Obtener usuario por ID inexistente debe lanzar excepción")
    void obtenerPorIdInexistente_DebeLanzarExcepcion() {
        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.obtenerPorId(999L));
    }

    @Test
    @DisplayName("Obtener todos los usuarios cargados por DataInit")
    void obtenerTodos_DebeRetornarUsuariosCargados() {
        List<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodos();

        assertNotNull(usuarios);
        assertTrue(usuarios.size() >= 5); // 5 usuarios cargados por DataInitConfig
    }

    @Test
    @DisplayName("Obtener usuarios por rol ESTUDIANTE")
    void obtenerPorRol_DebeRetornarEstudiantes() {
        List<UsuarioResponseDTO> estudiantes = usuarioService.obtenerPorRol(Rol.ESTUDIANTE);

        assertNotNull(estudiantes);
        assertTrue(estudiantes.size() >= 2); // 2 estudiantes cargados
        estudiantes.forEach(e -> assertEquals(Rol.ESTUDIANTE, e.getRol()));
    }

    @Test
    @DisplayName("RF-05: Obtener responsables activos")
    void obtenerResponsablesActivos_DebeRetornarActivos() {
        List<UsuarioResponseDTO> responsables = usuarioService.obtenerResponsablesActivos();

        assertNotNull(responsables);
        responsables.forEach(r -> {
            assertEquals(Rol.RESPONSABLE, r.getRol());
            assertTrue(r.getActivo());
        });
    }

    // ==================== ACTIVACIÓN / DESACTIVACIÓN ====================

    @Test
    @DisplayName("Desactivar usuario cambia su estado a inactivo")
    void desactivarUsuario_DebeCambiarEstado() {
        // Primero crear un usuario para no afectar los de prueba
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .identificacion("8888888888")
                .nombre("Para")
                .apellido("Desactivar")
                .email("para.desactivar@uq.edu.co")
                .rol(Rol.RESPONSABLE)
                .password("password123")
                .build();
        UsuarioResponseDTO creado = usuarioService.registrarUsuario(request);
        assertTrue(creado.getActivo());

        // Desactivar
        UsuarioResponseDTO desactivado = usuarioService.desactivarUsuario(creado.getId());
        assertFalse(desactivado.getActivo());

        // Reactivar
        UsuarioResponseDTO activado = usuarioService.activarUsuario(creado.getId());
        assertTrue(activado.getActivo());
    }
}
