package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.dto.usuario.UsuarioRequestDTO;
import co.edu.uniquindio.poo.dto.usuario.UsuarioResponseDTO;
import co.edu.uniquindio.poo.exception.OperacionNoPermitidaException;
import co.edu.uniquindio.poo.exception.ResourceNotFoundException;
import co.edu.uniquindio.poo.mapper.EntityMapper;
import co.edu.uniquindio.poo.model.entity.Usuario;
import co.edu.uniquindio.poo.model.enums.Rol;
import co.edu.uniquindio.poo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios del sistema.
 * RF-13: Control de acceso basado en roles.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EntityMapper mapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en el sistema.
     */
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO request) {
        // Validar que no exista otro usuario con la misma identificación o email
        if (usuarioRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new OperacionNoPermitidaException(
                    "Ya existe un usuario con la identificación: " + request.getIdentificacion());
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new OperacionNoPermitidaException(
                    "Ya existe un usuario con el email: " + request.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .identificacion(request.getIdentificacion())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .rol(request.getRol())
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        return mapper.toUsuarioDTO(usuario);
    }

    /**
     * Obtiene un usuario por su ID.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        return mapper.toUsuarioDTO(usuario);
    }

    /**
     * Obtiene todos los usuarios registrados.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(mapper::toUsuarioDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene usuarios por rol.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol).stream()
                .map(mapper::toUsuarioDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene responsables activos (para asignación).
     * RF-05: Solo responsables activos pueden ser asignados.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerResponsablesActivos() {
        return usuarioRepository.findByRolAndActivoTrue(Rol.RESPONSABLE).stream()
                .map(mapper::toUsuarioDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los usuarios activos (para seleccionar solicitante).
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(mapper::toUsuarioDTO)
                .collect(Collectors.toList());
    }

    /**
     * Desactiva un usuario del sistema.
     */
    public UsuarioResponseDTO desactivarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setActivo(false);
        usuario = usuarioRepository.save(usuario);
        return mapper.toUsuarioDTO(usuario);
    }

    /**
     * Activa un usuario del sistema.
     */
    public UsuarioResponseDTO activarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);
        return mapper.toUsuarioDTO(usuario);
    }

    /**
     * Busca un usuario por ID, lanza excepción si no existe.
     * Método interno reutilizable.
     */
    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Actualiza los datos de un usuario existente.
     */
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO request) {
        Usuario usuario = buscarUsuarioPorId(id);

        // Validar si el email pertenece a otro usuario
        if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new OperacionNoPermitidaException("Ya existe otro usuario con el email: " + request.getEmail());
        }

        // Validar si la identificacion pertenece a otro usuario
        if (!usuario.getIdentificacion().equals(request.getIdentificacion()) && usuarioRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new OperacionNoPermitidaException("Ya existe otro usuario con la identificación: " + request.getIdentificacion());
        }

        usuario.setIdentificacion(request.getIdentificacion());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());
        // No actualizamos el password aquí por seguridad, se requeriría otro endpoint si se desea cambiar.

        usuario = usuarioRepository.save(usuario);
        return mapper.toUsuarioDTO(usuario);
    }
}
