package co.edu.uniquindio.poo.dto.ia;

import co.edu.uniquindio.poo.model.enums.Prioridad;
import co.edu.uniquindio.poo.model.enums.TipoSolicitud;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para sugerencias de asistencia de IA.
 * RF-09 y RF-10.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerenciaIAResponseDTO {

    private Long solicitudId;
    private String tipoSugerido;
    private String prioridadSugerida;
    private String resumen;
    private String razonamiento;
    private String modeloUtilizado;
    private LocalDateTime generadoEn;
}
