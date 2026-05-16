package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.dto.solicitud.SolicitudResponseDTO;
import co.edu.uniquindio.poo.dto.ia.SugerenciaIAResponseDTO;
import co.edu.uniquindio.poo.model.enums.Prioridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para el servicio de asistencia IA.
 * RF-09: Generación de resúmenes y sugerencias.
 * RF-10: Sugerencia automática de clasificación.
 * RF-11: Sistema funcional sin IA.
 */
@ExtendWith(MockitoExtension.class)
class AsistenciaIAServiceTest {

    @InjectMocks
    private AsistenciaIAService asistenciaIAService;

    @Mock
    private SolicitudService solicitudService;

    private SolicitudResponseDTO crearSolicitudMock(Long id, String descripcion,
            String tipo, Prioridad prioridad,
            LocalDate fechaLimite) {
        return SolicitudResponseDTO.builder()
                .id(id)
                .descripcion(descripcion)
                .tipoSolicitud(tipo)
                .prioridad(prioridad)
                .fechaLimite(fechaLimite)
                .build();
    }

    // ==================== RF-10: SUGERENCIA DE TIPO ====================

    @Test
    @DisplayName("RF-10: Sugiere HOMOLOGACION cuando la descripción contiene 'homolog'")
    void descripcionConHomologacion_DebeSugerirHomologacion() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                1L, "Necesito homologar materias del programa anterior",
                null, null, null);
        when(solicitudService.obtenerPorId(1L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(1L);

        assertEquals("Homologación", resultado.getTipoSolicitudSugerido());
    }

    @Test
    @DisplayName("RF-10: Sugiere CANCELACION cuando la descripción contiene 'cancelar'")
    void descripcionConCancelacion_DebeSugerirCancelacion() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                2L, "Deseo cancelar la asignatura de física",
                null, null, null);
        when(solicitudService.obtenerPorId(2L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(2L);

        assertEquals("Cancelación de asignaturas", resultado.getTipoSolicitudSugerido());
    }

    @Test
    @DisplayName("RF-10: Sugiere SOLICITUD_CUPOS cuando la descripción contiene 'cupo'")
    void descripcionConCupo_DebeSugerirCupos() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                3L, "Solicito cupo adicional en el grupo de Base de Datos",
                null, null, null);
        when(solicitudService.obtenerPorId(3L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(3L);

        assertEquals("Solicitud de cupos", resultado.getTipoSolicitudSugerido());
    }

    @Test
    @DisplayName("RF-10: Sugiere REGISTRO_ASIGNATURAS cuando la descripción contiene 'registro' o 'matrícula'")
    void descripcionConRegistro_DebeSugerirRegistro() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                4L, "Necesito hacer el registro de asignaturas del semestre",
                null, null, null);
        when(solicitudService.obtenerPorId(4L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(4L);

        assertEquals("Registro de asignaturas", resultado.getTipoSolicitudSugerido());
    }

    @Test
    @DisplayName("RF-10: Si la descripción no coincide con ningún patrón, sugiere CONSULTA_ACADEMICA")
    void descripcionGenerica_DebeSugerirConsulta() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                5L, "Tengo una duda sobre el plan de estudios",
                null, null, null);
        when(solicitudService.obtenerPorId(5L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(5L);

        assertEquals("Consulta académica", resultado.getTipoSolicitudSugerido());
    }

    // ==================== RF-10: SUGERENCIA DE PRIORIDAD ====================

    @Test
    @DisplayName("RF-10: Fecha límite en 2 días o menos sugiere CRITICA")
    void fechaLimiteCercana_DebeSugerirCritica() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                6L, "Solicitud urgente",
                null, null, LocalDate.now().plusDays(1));
        when(solicitudService.obtenerPorId(6L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(6L);

        assertEquals(Prioridad.CRITICA, resultado.getPrioridadSugerida());
    }

    @Test
    @DisplayName("RF-10: Fecha límite en 5 días sugiere ALTA")
    void fechaLimiteMediana_DebeSugerirAlta() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                7L, "Solicitud normal",
                null, null, LocalDate.now().plusDays(4));
        when(solicitudService.obtenerPorId(7L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(7L);

        assertEquals(Prioridad.ALTA, resultado.getPrioridadSugerida());
    }

    @Test
    @DisplayName("RF-10: Descripción con 'urgente' sugiere ALTA")
    void descripcionUrgente_DebeSugerirAlta() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                8L, "Solicitud urgente necesito resolver hoy",
                null, null, null);
        when(solicitudService.obtenerPorId(8L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(8L);

        assertEquals(Prioridad.ALTA, resultado.getPrioridadSugerida());
    }

    // ==================== RF-09: JUSTIFICACIÓN Y MODELO ====================

    @Test
    @DisplayName("RF-09: La justificación siempre se genera y no está vacía")
    void justificacion_SiempreGenerada() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                9L, "Consulta general",
                null, null, null);
        when(solicitudService.obtenerPorId(9L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(9L);

        assertNotNull(resultado.getJustificacionIA());
        assertFalse(resultado.getJustificacionIA().isEmpty());
        assertNotNull(resultado.getModeloUtilizado());
        assertNotNull(resultado.getGeneradoEn());
    }

    // ==================== RF-11: FUNCIONAMIENTO INDEPENDIENTE ====================

    @Test
    @DisplayName("RF-11: Respeta tipo existente cuando ya está clasificada")
    void solicitudYaClasificada_DebeRespetarTipoExistente() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                10L, "Solicitud ya gestionada",
                "Homologación", Prioridad.ALTA, null);
        when(solicitudService.obtenerPorId(10L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerClasificacionSugerida(10L);

        // Debe respetar el tipo y prioridad ya asignados
        assertEquals("Homologación", resultado.getTipoSolicitudSugerido());
        assertEquals(Prioridad.ALTA, resultado.getPrioridadSugerida());
    }

    @Test
    @DisplayName("RF-11: Sugerencia completa incluye borrador de respuesta")
    void sugerenciaCompleta_IncluyeBorradorRespuesta() {
        SolicitudResponseDTO solicitud = crearSolicitudMock(
                11L, "Solicito sobrecupo para Ingeniería de Software",
                null, null, null);
        when(solicitudService.obtenerPorId(11L)).thenReturn(solicitud);

        SugerenciaIAResponseDTO resultado = asistenciaIAService.obtenerSugerencia(11L, null);

        assertNotNull(resultado.getSugerenciaRespuesta());
        assertFalse(resultado.getSugerenciaRespuesta().isEmpty());
        assertTrue(resultado.getSugerenciaRespuesta().contains("Estimado"));
    }
}
