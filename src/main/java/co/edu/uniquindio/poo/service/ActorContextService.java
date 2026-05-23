package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.exception.OperacionNoPermitidaException;
import co.edu.uniquindio.poo.model.entity.Usuario;
import co.edu.uniquindio.poo.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resuelve el actor autenticado desde el contexto de seguridad o sesión HTTP.
 * Evita recibir la identidad del usuario por body en operaciones transaccionales.
 */
@Service
@RequiredArgsConstructor
public class ActorContextService {

    private final UsuarioRepository usuarioRepository;

    public Usuario obtenerActorActual() {
        Usuario actorPorSecurity = obtenerDesdeSecurityContext();
        if (actorPorSecurity != null) {
            return actorPorSecurity;
        }

        Usuario actorPorSesion = obtenerDesdeSesionHttp();
        if (actorPorSesion != null) {
            return actorPorSesion;
        }

        throw new OperacionNoPermitidaException(
                "No se pudo determinar el actor autenticado desde el contexto de seguridad/sesión");
    }

    private Usuario obtenerDesdeSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }

        String identidad = principal instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : authentication.getName();

        if (identidad == null || identidad.isBlank()) {
            return null;
        }

        return buscarUsuarioPorIdentidad(identidad);
    }

    @SuppressWarnings("null")
    private Usuario obtenerDesdeSesionHttp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }

        HttpSession session = attributes.getRequest().getSession(false);
        if (session == null) {
            return null;
        }

        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId == null) {
            usuarioId = session.getAttribute("actorId");
        }

        if (usuarioId == null) {
            return null;
        }

        Long id = parseLongSeguro(usuarioId.toString());
        if (id == null) {
            return null;
        }

        return usuarioRepository.findById(id).orElse(null);
    }

    @SuppressWarnings("null")
    private Usuario buscarUsuarioPorIdentidad(String identidad) {
        return usuarioRepository.findByEmail(identidad)
                .or(() -> usuarioRepository.findByIdentificacion(identidad))
                .or(() -> {
                    Long id = parseLongSeguro(identidad);
                    return id == null ? java.util.Optional.empty() : usuarioRepository.findById(id);
                })
                .orElse(null);
    }

    private Long parseLongSeguro(String valor) {
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
