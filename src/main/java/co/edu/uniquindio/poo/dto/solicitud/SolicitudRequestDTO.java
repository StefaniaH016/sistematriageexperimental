package co.edu.uniquindio.poo.dto.solicitud;

import co.edu.uniquindio.poo.model.enums.CanalOrigen;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO para el registro de una nueva solicitud académica.
 * RF-01: Contiene los datos mínimos requeridos para crear una solicitud.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRequestDTO {

    @Size(max = 200, message = "El título no puede exceder los 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción de la solicitud es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "El canal de origen es obligatorio")
    private CanalOrigen canalOrigen;

    @NotNull(message = "El ID del solicitante es obligatorio")
    private Long solicitanteId;

    /** Fecha límite opcional asociada a la solicitud */
    private LocalDate fechaLimite;
}
