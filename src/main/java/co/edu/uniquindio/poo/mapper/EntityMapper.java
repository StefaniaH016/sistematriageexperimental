package co.edu.uniquindio.poo.mapper;

import co.edu.uniquindio.poo.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.poo.dto.solicitud.SolicitudResponseDTO;
import co.edu.uniquindio.poo.dto.usuario.UsuarioResponseDTO;
import co.edu.uniquindio.poo.model.entity.HistorialSolicitud;
import co.edu.uniquindio.poo.model.entity.Solicitud;
import co.edu.uniquindio.poo.model.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente encargado de mapear entidades a DTOs de respuesta.
 * Centraliza la conversión para mantener código limpio y reutilizable.
 */
@Component
public class EntityMapper {

    /**
     * Convierte una entidad Usuario a su DTO de respuesta.
     */
    public UsuarioResponseDTO toUsuarioDTO(Usuario usuario) {
        if (usuario == null) return null;
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .identificacion(usuario.getIdentificacion())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .build();
    }

    /**
     * Convierte una entidad HistorialSolicitud a su DTO de respuesta.
     */
    public HistorialResponseDTO toHistorialDTO(HistorialSolicitud historial) {
        if (historial == null) return null;
        return HistorialResponseDTO.builder()
                .id(historial.getId())
                .fechaHora(historial.getFechaHora())
                .accion(historial.getAccion())
                .usuario(toUsuarioDTO(historial.getUsuario()))
                .observaciones(historial.getObservaciones())
                .build();
    }

    /**
     * Convierte una entidad Solicitud a su DTO de respuesta completo.
     */
    public SolicitudResponseDTO toSolicitudDTO(Solicitud solicitud) {
        if (solicitud == null) return null;

        List<HistorialResponseDTO> historialDTOs = solicitud.getHistorial() != null
                ? solicitud.getHistorial().stream()
                    .map(this::toHistorialDTO)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return SolicitudResponseDTO.builder()
                .id(solicitud.getId())
                .titulo(solicitud.getTitulo())
                .tipoSolicitud(solicitud.getTipoSolicitud())
                .descripcion(solicitud.getDescripcion())
                .canalOrigen(solicitud.getCanalOrigen())
                .fechaRegistro(solicitud.getFechaRegistro())
                .estado(solicitud.getEstado())
                .prioridad(solicitud.getPrioridad())
                .justificacionPrioridad(solicitud.getJustificacionPrioridad())
                .fechaLimite(solicitud.getFechaLimite())
                .observacionCierre(solicitud.getObservacionCierre())
                .solicitante(toUsuarioDTO(solicitud.getSolicitante()))
                .responsable(toUsuarioDTO(solicitud.getResponsable()))
                .historial(historialDTOs)
                .build();
    }

    /**
     * Convierte una lista de solicitudes a sus DTOs.
     */
    public List<SolicitudResponseDTO> toSolicitudDTOList(List<Solicitud> solicitudes) {
        return solicitudes.stream()
                .map(this::toSolicitudDTO)
                .collect(Collectors.toList());
    }
}
