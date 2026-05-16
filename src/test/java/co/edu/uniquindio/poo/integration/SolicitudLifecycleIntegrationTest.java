package co.edu.uniquindio.poo.integration;

import co.edu.uniquindio.poo.dto.common.*;
import co.edu.uniquindio.poo.dto.solicitud.*;
import co.edu.uniquindio.poo.dto.usuario.*;
import co.edu.uniquindio.poo.dto.ia.*;
import co.edu.uniquindio.poo.model.enums.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración que recorre el ciclo de vida COMPLETO de una solicitud.
 * RF-01 → RF-02 → RF-03 → RF-04 → RF-05 → RF-06 → RF-07 → RF-08
 *
 * Flujo: REGISTRADA → CLASIFICADA → EN_ATENCION → ATENDIDA → CERRADA
 * Cada paso verifica el historial auditable (RF-06).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SolicitudLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final String ESTUDIANTE_EMAIL = "juan.perez@uq.edu.co";
    private static final String ESTUDIANTE_PASS = "123456";
    private static final String ADMIN_EMAIL = "ana.martinez@uq.edu.co";
    private static final String ADMIN_PASS = "admin123";
    private static final String RESPONSABLE_EMAIL = "carlos.lopez@uq.edu.co";
    private static final String RESPONSABLE_PASS = "123456";

    private Long solicitudId;

    // ==================== PASO 1: REGISTRO (RF-01) ====================

    @Test
    @Order(1)
    @DisplayName("Ciclo de vida - Paso 1: Registrar solicitud → REGISTRADA")
    void paso1_RegistrarSolicitud() throws Exception {
        SolicitudRequestDTO request = SolicitudRequestDTO.builder()
                .descripcion("Necesito cancelar la asignatura de Cálculo III por urgencia, "
                        + "cierre de matrícula inminente")
                .canalOrigen(CanalOrigen.CSU)
                .solicitanteId(1L)
                .fechaLimite(LocalDate.now().plusDays(3))
                .build();

        MvcResult result = mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.estado").value("REGISTRADA"))
                .andExpect(jsonPath("$.datos.canalOrigen").value("CSU"))
                .andExpect(jsonPath("$.datos.descripcion").isNotEmpty())
                .andExpect(jsonPath("$.datos.fechaRegistro").isNotEmpty())
                .andExpect(jsonPath("$.datos.solicitante.id").value(1))
                .andExpect(jsonPath("$.datos.historial").isArray())
                .andExpect(jsonPath("$.datos.historial.length()").value(1))
                .andReturn();

        solicitudId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("datos").get("id").asLong();
    }

    // ==================== PASO 2: CLASIFICACIÓN (RF-02 + RF-03) ====================

    @Test
    @Order(2)
    @DisplayName("Ciclo de vida - Paso 2: Clasificar + priorizar → CLASIFICADA")
    void paso2_ClasificarSolicitud() throws Exception {
        ClasificacionRequestDTO request = ClasificacionRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.CANCELACION_ASIGNATURAS.name())
                .observaciones("Clasificada como cancelación de asignaturas por solicitud del estudiante")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/clasificar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("CLASIFICADA"))
                .andExpect(jsonPath("$.datos.tipoSolicitud").value("CANCELACION_ASIGNATURAS"))
                .andExpect(jsonPath("$.datos.prioridad").exists())
                .andExpect(jsonPath("$.datos.justificacionPrioridad").isNotEmpty())
                // RF-06: Historial debe tener 2 entradas (registro + clasificación)
                .andExpect(jsonPath("$.datos.historial.length()").value(2));
    }

    // ==================== PASO 3: ASIGNACIÓN DE RESPONSABLE (RF-05) ====================

    @Test
    @Order(3)
    @DisplayName("Ciclo de vida - Paso 3: Asignar responsable")
    void paso3_AsignarResponsable() throws Exception {
        AsignacionRequestDTO request = AsignacionRequestDTO.builder()
                .responsableId(3L)   // Carlos López (RESPONSABLE)
                .observaciones("Se asigna al responsable Carlos López para atender la cancelación")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/asignar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.responsable.id").value(3))
                .andExpect(jsonPath("$.datos.responsable.nombre").value("Carlos"))
                // RF-06: Historial debe tener 3 entradas
                .andExpect(jsonPath("$.datos.historial.length()").value(3));
    }

    // ==================== PASO 4: EN ATENCIÓN (RF-04) ====================

    @Test
    @Order(4)
    @DisplayName("Ciclo de vida - Paso 4: Cambiar estado → EN_ATENCION")
    void paso4_CambiarAEnAtencion() throws Exception {
        CambioEstadoRequestDTO request = CambioEstadoRequestDTO.builder()
                .nuevoEstado(EstadoSolicitud.EN_ATENCION)
                .observaciones("El responsable comienza a atender la solicitud")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/estado")
                        .with(httpBasic(RESPONSABLE_EMAIL, RESPONSABLE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("EN_ATENCION"))
                .andExpect(jsonPath("$.datos.historial.length()").value(4));
    }

    // ==================== PASO 5: ATENDIDA (RF-04) ====================

    @Test
    @Order(5)
    @DisplayName("Ciclo de vida - Paso 5: Cambiar estado → ATENDIDA")
    void paso5_CambiarAAtendida() throws Exception {
        CambioEstadoRequestDTO request = CambioEstadoRequestDTO.builder()
                .nuevoEstado(EstadoSolicitud.ATENDIDA)
                .observaciones("Cancelación procesada exitosamente en el sistema académico")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/estado")
                        .with(httpBasic(RESPONSABLE_EMAIL, RESPONSABLE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("ATENDIDA"))
                .andExpect(jsonPath("$.datos.historial.length()").value(5));
    }

    // ==================== PASO 6: CIERRE (RF-08) ====================

    @Test
    @Order(6)
    @DisplayName("Ciclo de vida - Paso 6: Cerrar solicitud → CERRADA")
    void paso6_CerrarSolicitud() throws Exception {
        CierreRequestDTO request = CierreRequestDTO.builder()
                .observacionCierre("Solicitud atendida satisfactoriamente. "
                        + "Asignatura Cálculo III cancelada del plan del estudiante.")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/cerrar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("CERRADA"))
                .andExpect(jsonPath("$.datos.observacionCierre").isNotEmpty())
                // RF-06: Historial completo con 6 entradas
                .andExpect(jsonPath("$.datos.historial.length()").value(6));
    }

    // ==================== PASO 7: SOLICITUD CERRADA NO SE MODIFICA (RF-08) ====================

    @Test
    @Order(7)
    @DisplayName("Ciclo de vida - Paso 7: Solicitud cerrada NO permite modificaciones")
    void paso7_SolicitudCerradaInmutable() throws Exception {
        // Intentar reclasificar → debe fallar
        ClasificacionRequestDTO clasificacion = ClasificacionRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION.name())
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/clasificar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clasificacion)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exitoso").value(false));

        // Intentar repriorizar → debe fallar
        PriorizacionRequestDTO priorizacion = PriorizacionRequestDTO.builder()
                .prioridad(Prioridad.CRITICA)
                .justificacion("Intento de cambio en solicitud cerrada")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/priorizar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorizacion)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exitoso").value(false));

        // Intentar reasignar → debe fallar
        AsignacionRequestDTO asignacion = AsignacionRequestDTO.builder()
                .responsableId(3L)
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/asignar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(asignacion)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    // ==================== PASO 8: VERIFICAR HISTORIAL COMPLETO (RF-06) ====================

    @Test
    @Order(8)
    @DisplayName("Ciclo de vida - Paso 8: Historial completo con 6 acciones auditables")
    void paso8_VerificarHistorialCompleto() throws Exception {
        mockMvc.perform(get("/api/solicitudes/" + solicitudId)
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("CERRADA"))
                .andExpect(jsonPath("$.datos.historial.length()").value(6))
                // Verificar que cada entrada tiene los campos requeridos por RF-06
                .andExpect(jsonPath("$.datos.historial[0].fechaHora").exists())
                .andExpect(jsonPath("$.datos.historial[0].accion").exists())
                .andExpect(jsonPath("$.datos.historial[0].nombreUsuario").exists())
                // Primera acción = registro
                .andExpect(jsonPath("$.datos.historial[0].accion").value("Solicitud registrada"))
                // Última acción = cierre
                .andExpect(jsonPath("$.datos.historial[5].accion").value("Solicitud cerrada"));
    }

    // ==================== CONSULTAS CON FILTROS (RF-07) ====================

    @Test
    @Order(9)
    @DisplayName("RF-07: Consultar solicitudes cerradas")
    void consultarSolicitudesCerradas() throws Exception {
        mockMvc.perform(get("/api/solicitudes")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .param("estado", "CERRADA")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos").isArray())
                .andExpect(jsonPath("$.datos.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(10)
    @DisplayName("RF-07: Consultar solicitudes por tipo de solicitud")
    void consultarPorTipoSolicitud() throws Exception {
        mockMvc.perform(get("/api/solicitudes")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .param("tipo", "CANCELACION_ASIGNATURAS")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos").isArray());
    }
}
