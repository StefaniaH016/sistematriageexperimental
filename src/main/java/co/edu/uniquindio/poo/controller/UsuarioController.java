package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.dto.usuario.UsuarioRequestDTO;
import co.edu.uniquindio.poo.dto.common.ApiResponseDTO;
import co.edu.uniquindio.poo.dto.usuario.UsuarioResponseDTO;
import co.edu.uniquindio.poo.model.enums.Rol;
import co.edu.uniquindio.poo.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * RF-12: API REST para operaciones sobre usuarios.
 * RF-13: Control de acceso basado en roles.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario en el sistema.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> registrarUsuario(
            @Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.exitoso("Usuario registrado exitosamente", response));
    }

    /**
     * Obtiene un usuario por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> obtenerUsuario(@PathVariable Long id) {
        UsuarioResponseDTO response = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Usuario encontrado", response));
    }

    /**
     * Obtiene todos los usuarios.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UsuarioResponseDTO>>> obtenerTodos() {
        List<UsuarioResponseDTO> response = usuarioService.obtenerTodos();
        return ResponseEntity.ok(ApiResponseDTO.exitoso(
                "Se encontraron " + response.size() + " usuarios", response));
    }

    /**
     * Obtiene usuarios por rol.
     */
    @GetMapping("/rol/{rol}")
    public ResponseEntity<ApiResponseDTO<List<UsuarioResponseDTO>>> obtenerPorRol(@PathVariable Rol rol) {
        List<UsuarioResponseDTO> response = usuarioService.obtenerPorRol(rol);
        return ResponseEntity.ok(ApiResponseDTO.exitoso(
                "Usuarios con rol " + rol.getDescripcion(), response));
    }

    /**
     * Obtiene responsables activos (para asignación).
     * RF-05: Listar responsables disponibles.
     */
    @GetMapping("/responsables-activos")
    public ResponseEntity<ApiResponseDTO<List<UsuarioResponseDTO>>> obtenerResponsablesActivos() {
        List<UsuarioResponseDTO> response = usuarioService.obtenerResponsablesActivos();
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Responsables activos", response));
    }

    /**
     * Obtiene todos los usuarios activos (para seleccionar solicitante).
     */
    @GetMapping("/activos")
    public ResponseEntity<ApiResponseDTO<List<UsuarioResponseDTO>>> obtenerUsuariosActivos() {
        List<UsuarioResponseDTO> response = usuarioService.obtenerUsuariosActivos();
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Usuarios activos", response));
    }

    /**
     * Desactiva un usuario.
     */
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> desactivarUsuario(@PathVariable Long id) {
        UsuarioResponseDTO response = usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Usuario desactivado", response));
    }

    /**
     * Activa un usuario.
     */
    @PutMapping("/{id}/activar")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> activarUsuario(@PathVariable Long id) {
        UsuarioResponseDTO response = usuarioService.activarUsuario(id);
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Usuario activado", response));
    }

    /**
     * Actualiza un usuario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Usuario actualizado", response));
    }

    /**
     * Elimina lógicamente (desactiva) un usuario (Mapeo adicional para el frontend).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> eliminarUsuario(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Usuario desactivado correctamente", null));
    }
}
