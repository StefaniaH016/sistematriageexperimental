package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.model.entity.Solicitud;
import co.edu.uniquindio.poo.model.enums.Prioridad;
import co.edu.uniquindio.poo.model.enums.TipoSolicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el motor de reglas de priorización.
 * RF-03: Priorización basada en tipo de solicitud, impacto académico y fecha límite.
 */
class PriorizacionServiceTest {

    private PriorizacionService priorizacionService;

    @BeforeEach
    void setUp() {
        priorizacionService = new PriorizacionService();
    }

    @Test
    @DisplayName("RF-03: Registro de asignaturas con fecha límite cercana → Prioridad CRITICA")
    void registroAsignaturasConFechaLimiteCercana_DebeSerCritica() {
        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud("Registro de asignaturas")
                .fechaLimite(LocalDate.now().plusDays(1))
                .build();

        Object[] resultado = priorizacionService.calcularPrioridad(solicitud);
        assertEquals(Prioridad.CRITICA, resultado[0]);
        assertNotNull(resultado[1]); // Debe tener justificación
    }

    @Test
    @DisplayName("RF-03: Consulta académica sin fecha límite → Prioridad BAJA")
    void consultaAcademicaSinFechaLimite_DebeSerBaja() {
        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud("Consulta académica")
                .fechaLimite(null)
                .build();

        Object[] resultado = priorizacionService.calcularPrioridad(solicitud);
        assertEquals(Prioridad.BAJA, resultado[0]);
    }

    @Test
    @DisplayName("RF-03: Cancelación con fecha vencida → Prioridad CRITICA")
    void cancelacionConFechaVencida_DebeSerCritica() {
        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud("Cancelación de asignaturas")
                .fechaLimite(LocalDate.now().minusDays(1))
                .build();

        Object[] resultado = priorizacionService.calcularPrioridad(solicitud);
        assertEquals(Prioridad.CRITICA, resultado[0]);
    }

    @Test
    @DisplayName("RF-03: Homologación con fecha lejana → Prioridad MEDIA")
    void homologacionConFechaLejana_DebeSerMedia() {
        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud("Homologación")
                .fechaLimite(LocalDate.now().plusDays(30))
                .build();

        Object[] resultado = priorizacionService.calcularPrioridad(solicitud);
        // Homologación(3) + fecha lejana(1) = 4 → MEDIA
        assertEquals(Prioridad.MEDIA, resultado[0]);
    }

    @Test
    @DisplayName("RF-03: La justificación nunca es nula")
    void justificacionNuncaEsNula() {
        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud("Solicitud de cupos")
                .build();

        Object[] resultado = priorizacionService.calcularPrioridad(solicitud);
        assertNotNull(resultado[1]);
        assertFalse(((String) resultado[1]).isEmpty());
    }

    @Test
    @DisplayName("RF-03: Señales semánticas de urgencia elevan la prioridad")
    void descripcionConUrgencia_DebeElevarPrioridad() {
        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud("Consulta académica")
                .descripcion("Caso urgente por cierre de matricula inminente")
                .build();

        Object[] resultado = priorizacionService.calcularPrioridad(solicitud);
        assertEquals(Prioridad.MEDIA, resultado[0]);
    }
}
