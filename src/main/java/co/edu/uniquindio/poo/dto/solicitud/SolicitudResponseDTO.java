package co.edu.uniquindio.poo.dto.solicitud;

import co.edu.uniquindio.poo.model.enums.*;
import co.edu.uniquindio.poo.dto.usuario.UsuarioResponseDTO;
import co.edu.uniquindio.poo.dto.historial.HistorialResponseDTO;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con la información completa de una solicitud.
 * RF-07: Consulta de solicitudes con toda la información relevante.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResponseDTO {

    private Long id;
    private String titulo;
    private String tipoSolicitud;
    private String descripcion;
    private CanalOrigen canalOrigen;
    private LocalDateTime fechaRegistro;
    private EstadoSolicitud estado;
    private Prioridad prioridad;
    private String justificacionPrioridad;
    private LocalDate fechaLimite;
    private String observacionCierre;

    /** Información del solicitante */
    private UsuarioResponseDTO solicitante;

    /** Información del responsable asignado (puede ser null) */
    private UsuarioResponseDTO responsable;

    /** Historial de acciones sobre la solicitud */
    private List<HistorialResponseDTO> historial;
}
