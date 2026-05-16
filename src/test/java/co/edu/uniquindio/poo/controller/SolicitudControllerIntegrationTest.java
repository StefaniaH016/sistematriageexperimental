package co.edu.uniquindio.poo.controller;

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
 * Tests de integración para la API REST de solicitudes.
 * Verifica el flujo completo del ciclo de vida de una solicitud.
 * Usa httpBasic() para autenticación real contra SecurityConfig y ActorContextService.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SolicitudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // Credenciales de usuarios cargados por DataInitConfig
    private static final String ESTUDIANTE_EMAIL = "juan.perez@uq.edu.co";
    private static final String ESTUDIANTE_PASS = "123456";
    private static final String ADMIN_EMAIL = "ana.martinez@uq.edu.co";
    private static final String ADMIN_PASS = "admin123";
    private static final String RESPONSABLE_EMAIL = "carlos.lopez@uq.edu.co";
    private static final String RESPONSABLE_PASS = "123456";

    // ==================== RF-01: REGISTRO ====================

    @Test
    @Order(1)
    @DisplayName("RF-01: Registrar solicitud exitosamente (autenticado como estudiante)")
    void registrarSolicitud_DebeRetornarCreated() throws Exception {
        SolicitudRequestDTO request = SolicitudRequestDTO.builder()
                .descripcion("Necesito registrar la asignatura Programación Avanzada para este semestre")
                .canalOrigen(CanalOrigen.CSU)
                .solicitanteId(1L)
                .build();

        mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.estado").value("REGISTRADA"))
                .andExpect(jsonPath("$.datos.canalOrigen").value("CSU"))
                .andExpect(jsonPath("$.datos.historial").isArray())
                .andExpect(jsonPath("$.datos.historial[0].accion").value("Solicitud registrada"));
    }

    @Test
    @Order(2)
    @DisplayName("RF-01: Registrar solicitud sin descripción debe fallar")
    void registrarSolicitudSinDescripcion_DebeRetornarBadRequest() throws Exception {
        SolicitudRequestDTO request = SolicitudRequestDTO.builder()
                .canalOrigen(CanalOrigen.CORREO)
                .solicitanteId(1L)
                .build();

        mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("RF-01: Registrar solicitud con fecha límite pasada debe fallar")
    void registrarSolicitudConFechaLimitePasada_DebeRetornarBadRequest() throws Exception {
        SolicitudRequestDTO request = SolicitudRequestDTO.builder()
                .descripcion("Solicitud con fecha no valida para registro")
                .canalOrigen(CanalOrigen.CORREO)
                .solicitanteId(1L)
                .fechaLimite(LocalDate.now().minusDays(1))
                .build();

        mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    // ==================== RF-02: CLASIFICACIÓN ====================

    @Test
    @Order(4)
    @DisplayName("RF-02 + RF-03: Clasificar solicitud asigna tipo y calcula prioridad")
    void clasificarSolicitud_DebeAsignarTipoYPrioridad() throws Exception {
        // Primero crear solicitud como estudiante
        SolicitudRequestDTO createRequest = SolicitudRequestDTO.builder()
                .descripcion("Solicitud de homologación de materias del programa anterior")
                .canalOrigen(CanalOrigen.CORREO)
                .solicitanteId(1L)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long solicitudId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("datos").get("id").asLong();

        // Clasificar como administrativo
        ClasificacionRequestDTO clasificacionRequest = ClasificacionRequestDTO.builder()
                .tipoSolicitud("Homologación")
                .observaciones("Clasificada como homologación")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/clasificar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clasificacionRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("CLASIFICADA"))
                .andExpect(jsonPath("$.datos.tipoSolicitud").value("Homologación"))
                .andExpect(jsonPath("$.datos.prioridad").exists())
                .andExpect(jsonPath("$.datos.justificacionPrioridad").exists());
    }

    // ==================== RF-04: TRANSICIÓN INVÁLIDA ====================

    @Test
    @Order(5)
    @DisplayName("RF-04: Transición inválida debe ser rechazada")
    void transicionInvalida_DebeRetornarBadRequest() throws Exception {
        // Crear solicitud
        SolicitudRequestDTO createRequest = SolicitudRequestDTO.builder()
                .descripcion("Consulta sobre plan de estudios")
                .canalOrigen(CanalOrigen.TELEFONICO)
                .solicitanteId(1L)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long solicitudId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("datos").get("id").asLong();

        // Intentar saltar de REGISTRADA a EN_ATENCION (inválido)
        CambioEstadoRequestDTO cambioEstado = CambioEstadoRequestDTO.builder()
                .nuevoEstado(EstadoSolicitud.EN_ATENCION)
                .observaciones("Intentando saltar estado")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/estado")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambioEstado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    // ==================== RF-05: ASIGNACIÓN DE RESPONSABLE ====================

    @Test
    @Order(6)
    @DisplayName("RF-05: Asignar responsable activo a solicitud")
    void asignarResponsable_DebeRegistrarEnHistorial() throws Exception {
        // Crear y clasificar solicitud
        SolicitudRequestDTO createRequest = SolicitudRequestDTO.builder()
                .descripcion("Solicitud de cupos para Bases de Datos")
                .canalOrigen(CanalOrigen.SAC)
                .solicitanteId(1L)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long solicitudId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("datos").get("id").asLong();

        // Asignar responsable (ID 3 = Carlos López, RESPONSABLE)
        AsignacionRequestDTO asignacionRequest = AsignacionRequestDTO.builder()
                .responsableId(3L)
                .observaciones("Asignado al responsable de turno")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/asignar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(asignacionRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.responsable.id").value(3))
                .andExpect(jsonPath("$.datos.responsable.nombre").value("Carlos"));
    }

    // ==================== RF-07: CONSULTAS ====================

    @Test
    @Order(7)
    @DisplayName("RF-07: Consultar todas las solicitudes")
    void consultarTodas_DebeRetornarLista() throws Exception {
        mockMvc.perform(get("/api/solicitudes")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("RF-07: Consultar solicitudes por estado")
    void consultarPorEstado_DebeRetornarFiltradas() throws Exception {
        mockMvc.perform(get("/api/solicitudes/estado/REGISTRADA")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    @Order(9)
    @DisplayName("RF-07: Consultar con filtro de estado")
    void consultarConFiltros_DebeRetornarFiltradas() throws Exception {
        mockMvc.perform(get("/api/solicitudes")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .param("estado", "REGISTRADA")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));
    }

    @Test
    @Order(10)
    @DisplayName("RF-07: Consultar solicitudes paginadas")
    void consultarSolicitudesPaginadas_DebeRetornarEstructuraPaginada() throws Exception {
        mockMvc.perform(get("/api/solicitudes/paginado")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.contenido").isArray())
                .andExpect(jsonPath("$.datos.numeroPagina").value(0))
                .andExpect(jsonPath("$.datos.tamanoPagina").value(2));
    }

    // ==================== USUARIOS ====================

    @Test
    @Order(11)
    @DisplayName("Obtener todos los usuarios (autenticado como administrativo)")
    void obtenerUsuarios_DebeRetornarListaCargada() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    @Order(12)
    @DisplayName("RF-05: Obtener responsables activos para asignación")
    void obtenerResponsablesActivos_DebeRetornarLista() throws Exception {
        mockMvc.perform(get("/api/usuarios/responsables-activos")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));
    }

    // ==================== RF-08: CIERRE ====================

    @Test
    @Order(13)
    @DisplayName("RF-08: Cerrar solicitud requiere estado ATENDIDA")
    void cerrarSolicitudNoAtendida_DebeRetornarBadRequest() throws Exception {
        // Crear solicitud en estado REGISTRADA
        SolicitudRequestDTO createRequest = SolicitudRequestDTO.builder()
                .descripcion("Consulta sobre requisitos de grado")
                .canalOrigen(CanalOrigen.PRESENCIAL)
                .solicitanteId(1L)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/solicitudes")
                        .with(httpBasic(ESTUDIANTE_EMAIL, ESTUDIANTE_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long solicitudId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("datos").get("id").asLong();

        // Intentar cerrar sin estar en ATENDIDA (debe fallar)
        CierreRequestDTO cierreRequest = CierreRequestDTO.builder()
                .observacionCierre("Intento de cierre prematuro")
                .build();

        mockMvc.perform(put("/api/solicitudes/" + solicitudId + "/cerrar")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cierreRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));
    }
}
