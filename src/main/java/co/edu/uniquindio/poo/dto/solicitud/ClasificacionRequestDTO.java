package co.edu.uniquindio.poo.dto.solicitud;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para clasificar una solicitud según su tipo.
 * RF-02: Clasificación de solicitudes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificacionRequestDTO {

    @NotNull(message = "El tipo de solicitud es obligatorio")
    private String tipoSolicitud;

    /** Observaciones opcionales sobre la clasificación */
    private String observaciones;
}
