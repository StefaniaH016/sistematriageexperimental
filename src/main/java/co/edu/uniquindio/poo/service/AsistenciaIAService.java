package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.dto.ia.SugerenciaIARequestDTO;
import co.edu.uniquindio.poo.dto.solicitud.SolicitudResponseDTO;
import co.edu.uniquindio.poo.dto.ia.SugerenciaIAResponseDTO;
import co.edu.uniquindio.poo.model.enums.Prioridad;
import co.edu.uniquindio.poo.model.enums.TipoSolicitud;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de asistencia inteligente utilizando la API de Gemini.
 * Cumple con RF-09 y RF-10 (Asistencia IA opcional).
 */
@Service
@RequiredArgsConstructor
public class AsistenciaIAService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final SolicitudService solicitudService;
    private final RestTemplate restTemplate = new RestTemplate();

    public SugerenciaIAResponseDTO obtenerSugerencia(Long solicitudId, SugerenciaIARequestDTO request) {
        SolicitudResponseDTO solicitud = solicitudService.obtenerPorId(solicitudId);

        // RF-11: Aplicar reglas de negocio primero (funciona sin IA)
        SugerenciaIAResponseDTO sugerenciaBase = aplicarReglasNegocio(solicitud);

        // RF-11: Verificar configuración de la IA
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.contains("${")) {
            sugerenciaBase.setJustificacionIA("Sugerencia basada en análisis de contenido (IA no configurada).");
            sugerenciaBase.setModeloUtilizado("REGLAS_NEGOCIO");
            sugerenciaBase.setGeneradoEn(LocalDateTime.now());
            return sugerenciaBase;
        }

        try {
            String prompt = buildPrompt(solicitud, request != null ? request.getContextoAdicional() : null);
            String respuestaIARaw = llamarGeminiAPI(prompt);

            // Limpiar posible formato markdown de la respuesta
            String jsonContent = respuestaIARaw;
            if (jsonContent.contains("```json")) {
                jsonContent = jsonContent.substring(jsonContent.indexOf("```json") + 7);
                if (jsonContent.contains("```")) {
                    jsonContent = jsonContent.substring(0, jsonContent.indexOf("```"));
                }
            } else if (jsonContent.contains("```")) {
                jsonContent = jsonContent.substring(jsonContent.indexOf("```") + 3);
                if (jsonContent.contains("```")) {
                    jsonContent = jsonContent.substring(0, jsonContent.indexOf("```"));
                }
            }
            jsonContent = jsonContent.trim();

            JSONObject jsonRes = new JSONObject(jsonContent);

            // Mapeo seguro de la respuesta
            String tipoSugeridoRaw = jsonRes.optString("tipoSolicitudSugerido",
                    sugerenciaBase.getTipoSolicitudSugerido());
            String prioridadRaw = jsonRes.optString("prioridadSugerida", sugerenciaBase.getPrioridadSugerida().name());

            return SugerenciaIAResponseDTO.builder()
                    .solicitudId(solicitudId)
                    .tipoSolicitudSugerido(tipoSugeridoRaw)
                    .prioridadSugerida(parsePrioridad(prioridadRaw, sugerenciaBase.getPrioridadSugerida()))
                    .resumenHistorial(jsonRes.optString("resumenHistorial", sugerenciaBase.getResumenHistorial()))
                    .sugerenciaClasificar(
                            jsonRes.optString("sugerenciaClasificar", sugerenciaBase.getSugerenciaClasificar()))
                    .sugerenciaAsignar(
                            jsonRes.optString("sugerenciaAsignar", sugerenciaBase.getSugerenciaAsignar()))
                    .sugerenciaEnAtencion(
                            jsonRes.optString("sugerenciaEnAtencion", sugerenciaBase.getSugerenciaEnAtencion()))
                    .sugerenciaAtendida(jsonRes.optString("sugerenciaAtendida", sugerenciaBase.getSugerenciaAtendida()))
                    .sugerenciaCierre(jsonRes.optString("sugerenciaCierre", sugerenciaBase.getSugerenciaCierre()))
                    .justificacionIA(jsonRes.optString("justificacionIA", sugerenciaBase.getJustificacionIA()))
                    .modeloUtilizado("Gemini-1.5-Flash")
                    .generadoEn(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // Fallback: Si falla la IA, devolvemos la sugerencia basada en reglas (RF-11)
            System.err.println("Error llamando a Gemini: " + e.getMessage());
            sugerenciaBase.setJustificacionIA(
                    "Sugerencia automática basada en reglas de negocio. (Nota: El servicio de IA se encuentra temporalmente saturado o no disponible).");
            sugerenciaBase.setModeloUtilizado("REGLAS_NEGOCIO");
            sugerenciaBase.setGeneradoEn(LocalDateTime.now());
            return sugerenciaBase;
        }
    }

    private SugerenciaIAResponseDTO aplicarReglasNegocio(SolicitudResponseDTO solicitud) {
        String content = ((solicitud.getTitulo() != null ? solicitud.getTitulo() + " " : "")
                + (solicitud.getDescripcion() != null ? solicitud.getDescripcion() : "")).toLowerCase();

        String tipoSugerido = TipoSolicitud.CONSULTA_ACADEMICA.name();
        Prioridad prioridadSugerida = Prioridad.MEDIA;

        boolean yaClasificada = false;
        if (solicitud.getTipoSolicitud() != null && !solicitud.getTipoSolicitud().isEmpty()) {
            for (TipoSolicitud t : TipoSolicitud.values()) {
                String descNorm = t.getDescripcion().toLowerCase();
                String reqNorm = solicitud.getTipoSolicitud().toLowerCase();
                if (t.getDescripcion().equalsIgnoreCase(solicitud.getTipoSolicitud())
                        || t.name().equalsIgnoreCase(solicitud.getTipoSolicitud())
                        || descNorm.contains(reqNorm)
                        || reqNorm.contains(descNorm)) {
                    tipoSugerido = t.name();
                    yaClasificada = true;
                    break;
                }
            }
        }

        boolean yaPriorizada = false;
        if (solicitud.getPrioridad() != null) {
            prioridadSugerida = solicitud.getPrioridad();
            yaPriorizada = true;
        }

        // --- Reglas de clasificación por palabras clave (RF-02 extendido) ---
        if (!yaClasificada) {
            // Cancelación de semestre (debe ir antes que cancelación de asignaturas)
            if (content.contains("semestre")
                    && (content.contains("cancel") || content.contains("retir") || content.contains("abandon"))) {
                tipoSugerido = TipoSolicitud.CANCELACION_SEMESTRE.name();
                prioridadSugerida = Prioridad.ALTA;
            }
            // Modificación de matrícula
            else if ((content.contains("modifica") || content.contains("cambio"))
                    && (content.contains("matrícula") || content.contains("matricula"))) {
                tipoSugerido = TipoSolicitud.MODIFICACION_MATRICULA.name();
                prioridadSugerida = Prioridad.ALTA;
            }
            // Registro de asignaturas
            else if (content.contains("registro") && (content.contains("asignatura") || content.contains("materia"))) {
                tipoSugerido = TipoSolicitud.REGISTRO_ASIGNATURAS.name();
            }
            // Cancelación de asignaturas
            else if ((content.contains("cancel") || content.contains("retir"))
                    && (content.contains("asignatura") || content.contains("materia"))) {
                tipoSugerido = TipoSolicitud.CANCELACION_ASIGNATURAS.name();
            }
            // Homologación y reconocimiento de créditos
            else if (content.contains("homologa") || content.contains("reconocimiento")
                    || content.contains("reconocimiento de crédito") || content.contains("reconocimiento de credito")
                    || content.contains("créditos adicionales")) {
                tipoSugerido = TipoSolicitud.HOMOLOGACION.name();
            }
            // Examen supletorio o habilitación
            else if (content.contains("supletorio") || content.contains("examen") || content.contains("perdi")
                    || content.contains("examen supletorio") || content.contains("habilitación")
                    || content.contains("habilitacion") || content.contains("habilitar")
                    || content.contains("recuperar") || content.contains("recuperación")) {
                tipoSugerido = TipoSolicitud.EXAMEN_SUPLETORIO.name();
                prioridadSugerida = Prioridad.ALTA;
            }
            // Cupos
            else if (content.contains("cupo") || content.contains("sobrecupo")) {
                tipoSugerido = TipoSolicitud.SOLICITUD_CUPOS.name();
            }
            // Apoyo económico / becas
            else if (content.contains("apoyo") || content.contains("económico") || content.contains("economico")
                    || content.contains("subsidio") || content.contains("beca") || content.contains("descuento")
                    || content.contains("media beca")) {
                tipoSugerido = TipoSolicitud.APOYO_ECONOMICO.name();
            }
            // Apoyo psicosocial
            else if (content.contains("psicolog") || content.contains("bienestar") || content.contains("ansiedad")
                    || content.contains("estrés") || content.contains("salud mental")) {
                tipoSugerido = TipoSolicitud.APOYO_PSICOSOCIAL.name();
            }
            // Trabajo de grado o práctica empresarial
            else if (content.contains("trabajo de grado") || content.contains("tesis") || content.contains("proyecto de grado")
                    || content.contains("práctica") || content.contains("practica") || content.contains("pasantía")
                    || content.contains("pasantia")) {
                tipoSugerido = TipoSolicitud.TRABAJO_GRADO.name();
            }
            // Certificados y documentos académicos
            else if (content.contains("certificado") || content.contains("constancia") || content.contains("paz y salvo")
                    || content.contains("paz&salvo") || content.contains("acta") || content.contains("grado")
                    || content.contains("graduac")) {
                tipoSugerido = TipoSolicitud.CERTIFICADO_ESTUDIOS.name();
            }
            // Reingreso y reserva de cupo
            else if (content.contains("reserva") && content.contains("cupo")) {
                tipoSugerido = TipoSolicitud.RESERVA_CUPO.name();
            } else if (content.contains("reingreso") || content.contains("volver") || content.contains("retomar")) {
                tipoSugerido = TipoSolicitud.REINGRESO.name();
            }
            // Transferencia interna
            else if (content.contains("transferencia") || content.contains("cambio de programa")
                    || content.contains("cambio de carrera")) {
                tipoSugerido = TipoSolicitud.TRANSFERENCIA_INTERNA.name();
            }
            // Proceso disciplinario
            else if (content.contains("disciplinar") || content.contains("apelación") || content.contains("apelacion")
                    || content.contains("sanc")) {
                tipoSugerido = TipoSolicitud.PROCESO_DISCIPLINARIO.name();
                prioridadSugerida = Prioridad.ALTA;
            }
        }

        if (!yaPriorizada) {
            if (content.contains("urgente") || content.contains("auxilio") || content.contains("inmediato")
                    || content.contains("inminente")) {
                prioridadSugerida = Prioridad.ALTA;
            }
            // Reglas de prioridad por fecha o urgencia (RF-03)
            if (solicitud.getFechaLimite() != null) {
                long dias = ChronoUnit.DAYS.between(LocalDate.now(), solicitud.getFechaLimite());
                if (dias <= 2)
                    prioridadSugerida = Prioridad.CRITICA;
                else if (dias <= 5 && prioridadSugerida != Prioridad.CRITICA)
                    prioridadSugerida = Prioridad.ALTA;
            }
        }

        String descripcionTipo = TipoSolicitud.valueOf(tipoSugerido).getDescripcion();

        // Construir justificación narrativa de la prioridad basada en reglas
        String justificacionPrioridad = construirJustificacionReglas(solicitud, prioridadSugerida, descripcionTipo);

        return SugerenciaIAResponseDTO.builder()
                .solicitudId(solicitud.getId())
                .tipoSolicitudSugerido(descripcionTipo)
                .prioridadSugerida(prioridadSugerida)
                .resumenHistorial("Solicitud en estado " + solicitud.getEstado() + " con "
                        + (solicitud.getHistorial() != null ? solicitud.getHistorial().size() : 0)
                        + " acciones previas.")
                .sugerenciaClasificar("Se sugiere clasificar como " + descripcionTipo + " basado en análisis del contenido.")
                .sugerenciaAsignar("Se asigna responsable para atender la solicitud de tipo '" + descripcionTipo
                        + "'. La prioridad " + prioridadSugerida.name() + " requiere atención oportuna.")
                .sugerenciaEnAtencion(
                        "Estimado estudiante, su solicitud ha sido asignada a un responsable y está en revisión.")
                .sugerenciaAtendida("Estimado estudiante, hemos atendido su solicitud sobre " + descripcionTipo
                        + ". Quedamos a la espera de sus comentarios.")
                .sugerenciaCierre("La solicitud ha sido resuelta satisfactoriamente y se procede a su cierre.")
                .justificacionIA(justificacionPrioridad)
                .build();
    }

    public SugerenciaIAResponseDTO obtenerClasificacionSugerida(Long solicitudId) {
        return obtenerSugerencia(solicitudId, null);
    }

    /**
     * Construye una justificación narrativa de la prioridad basada en las reglas de negocio.
     * Esta justificación es la que se guarda en el campo justificacionPrioridad de la solicitud.
     */
    private String construirJustificacionReglas(SolicitudResponseDTO solicitud, Prioridad prioridad, String tipo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Prioridad ").append(prioridad.name()).append(" asignada por el sistema: ");

        // Razón por tipo
        switch (prioridad) {
            case CRITICA:
                sb.append("El tipo de solicitud '").append(tipo).append("' presenta un nivel crítico de impacto académico");
                break;
            case ALTA:
                sb.append("El tipo de solicitud '").append(tipo).append("' tiene alto impacto en el proceso académico del estudiante");
                break;
            case MEDIA:
                sb.append("El tipo de solicitud '").append(tipo).append("' tiene un impacto moderado en el proceso académico");
                break;
            default:
                sb.append("El tipo de solicitud '").append(tipo).append("' no presenta urgencia académica inmediata");
        }

        // Razón por fecha
        if (solicitud.getFechaLimite() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), solicitud.getFechaLimite());
            if (dias < 0) {
                sb.append(". La fecha límite ya venció, lo que hace urgente una resolución inmediata");
            } else if (dias <= 2) {
                sb.append(". La fecha límite vence en menos de 2 días, aumentando la urgencia");
            } else if (dias <= 5) {
                sb.append(". La fecha límite es en menos de 5 días, requiriendo atención preferente");
            } else if (dias <= 10) {
                sb.append(". La fecha límite está próxima (menos de 10 días)");
            }
        }

        // Razón por descripción
        if (solicitud.getDescripcion() != null) {
            String desc = solicitud.getDescripcion().toLowerCase();
            if (desc.contains("urgente") || desc.contains("inminente")) {
                sb.append(". La solicitud contiene indicadores de urgencia explícitos en su descripción");
            }
        }

        sb.append(".");
        return sb.toString();
    }

    private String llamarGeminiAPI(String prompt) {
        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);

            HttpEntity<String> requestEntity = new HttpEntity<>(body.toString(), headers);
            String response = restTemplate.postForObject(url, requestEntity, String.class);

            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            throw new RuntimeException("Fallo en la comunicación con Gemini API: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(SolicitudResponseDTO solicitud, String contexto) {
        StringBuilder historialStr = new StringBuilder();
        if (solicitud.getHistorial() != null && !solicitud.getHistorial().isEmpty()) {
            historialStr.append("\nHISTORIAL DE ACCIONES:\n");
            for (var h : solicitud.getHistorial()) {
                historialStr.append("- [").append(h.getFechaHora()).append("] ").append(h.getAccion()).append(": ")
                        .append(h.getObservaciones()).append("\n");
            }
        }

        return "Actúa como un asistente académico experto de la Universidad del Quindío. Analiza la siguiente solicitud de un estudiante y genera una respuesta estructurada estrictamente en formato JSON.\n\n"
                +
                "DETALLES DE LA SOLICITUD:\n" +
                "- Título: "
                + (solicitud.getTitulo() != null ? solicitud.getTitulo() : "Sin título") + "\n" +
                "- Descripción: "
                + (solicitud.getDescripcion() != null ? solicitud.getDescripcion() : "Sin descripción") + "\n" +
                "- Fecha de registro: " + solicitud.getFechaRegistro() + "\n" +
                "- Fecha límite asociada: "
                + (solicitud.getFechaLimite() != null ? solicitud.getFechaLimite() : "Ninguna") + "\n" +
                historialStr.toString() +
                (contexto != null ? "- Contexto adicional del administrativo: " + contexto + "\n" : "") +
                "\nTIPOS DE SOLICITUD DISPONIBLES (debes elegir el más adecuado y devolver su nombre EXACTO en inglés):\n"
                +
                "- REGISTRO_ASIGNATURAS: Para inscripción de materias\n" +
                "- CANCELACION_ASIGNATURAS: Para cancelar materias individuales\n" +
                "- CANCELACION_SEMESTRE: Para cancelar el semestre completo o retirarse temporalmente\n" +
                "- SOLICITUD_CUPOS: Para pedir un cupo en una materia llena\n" +
                "- HOMOLOGACION: Para reconocer materias de otra institución y reconocimiento de créditos adicionales\n" +
                "- EXAMEN_SUPLETORIO: Para presentar un examen no presentado por inasistencia o habilitación de materia perdida\n" +
                "- MODIFICACION_MATRICULA: Para modificar la matrícula económica o académica\n" +
                "- RESERVA_CUPO: Para reservar un cupo para el siguiente semestre\n" +
                "- REINGRESO: Para volver después de haberse retirado\n" +
                "- TRANSFERENCIA_INTERNA: Para cambiar de programa o facultad\n" +
                "- CERTIFICADO_ESTUDIOS: Para solicitar constancias, certificados, paz y salvo, o actas de grado\n" +
                "- APOYO_ECONOMICO: Para solicitar subsidios, apoyo económico, becas o descuentos en matrícula\n" +
                "- APOYO_PSICOSOCIAL: Para solicitar acompañamiento psicológico y bienestar\n" +
                "- TRABAJO_GRADO: Para trámites relacionados con trabajo de grado, tesis o prácticas/pasantías empresariales\n" +
                "- PROCESO_DISCIPLINARIO: Para apelaciones o procesos disciplinarios\n" +
                "- CONSULTA_ACADEMICA: Para consultas generales que no encajan en otras categorías\n" +
                "\nIMPORTANTE: Si la solicitud menciona algo que no existe textualmente (como 'cancelar semestre'),\n" +
                "debes razonar y elegir la categoría más apropiada (en este caso: CANCELACION_SEMESTRE).\n" +
                "\nREQUISITOS DE SALIDA (JSON):\n" +
                "Debes responder Únicamente con un objeto JSON con los siguientes campos:\n" +
                "1. \"tipoSolicitudSugerido\": El nombre exacto del tipo (ej: CANCELACION_SEMESTRE). DEBE ser uno de la lista anterior.\n"
                +
                "2. \"prioridadSugerida\": Nivel de urgencia. DEBE ser exactamente uno de: [CRITICA, ALTA, MEDIA, BAJA].\n"
                +
                "3. \"resumenHistorial\": Un resumen de TODO el proceso actual (desde la creacion hasta el estado actual), incluyendo las fechas importantes.\n"
                +
                "4. \"sugerenciaClasificar\": Borrador corto para la observación al clasificar la solicitud.\n" +
                "5. \"sugerenciaAsignar\": Observación breve que el administrativo puede registrar al momento de asignar un responsable, indicando la razón de la asignación y los próximos pasos esperados.\n" +
                "6. \"sugerenciaEnAtencion\": Borrador de respuesta cordial al estudiante indicando que la solicitud está en revisión por un responsable.\n"
                +
                "6. \"sugerenciaAtendida\": Borrador de respuesta al estudiante indicando la solución o atención de su solicitud.\n"
                +
                "7. \"sugerenciaCierre\": Borrador de conclusión para cerrar formalmente la solicitud.\n" +
                "8. \"justificacionIA\": Explicación detallada y en español del MOTIVO CONCRETO por el que se asigna esa prioridad a esta solicitud específica. Debe mencionar: el tipo de trámite, el impacto académico real para el estudiante, y si hay fecha límite, cuánto tiempo queda. Escribe como si le explicaras al administrativo por qué debe atender esto con esa urgencia. Mínimo 2 oraciones claras y concretas.\n" +
                "\nEJEMPLO:\n" +
                "{\n" +
                "  \"tipoSolicitudSugerido\": \"CANCELACION_SEMESTRE\",\n" +
                "  \"prioridadSugerida\": \"ALTA\",\n" +
                "  \"resumenHistorial\": \"La solicitud fue registrada hoy y no tiene acciones previas.\",\n" +
                "  \"sugerenciaClasificar\": \"Clasificada como cancelación de semestre.\",\n" +
                "  \"sugerenciaEnAtencion\": \"Estimado estudiante, su solicitud de cancelación de semestre ha sido recibida y se encuentra en revisión.\",\n"
                +
                "  \"sugerenciaAtendida\": \"Estimado estudiante, su cancelación ha sido tramitada correctamente.\",\n"
                +
                "  \"sugerenciaCierre\": \"Trámite finalizado exitosamente. Se procede al cierre.\",\n" +
                "  \"justificacionIA\": \"El estudiante solicita cancelar el semestre por motivos personales.\"\n" +
                "}";
    }

    private Prioridad parsePrioridad(String raw, Prioridad fallback) {
        if (raw == null || raw.isEmpty())
            return fallback;
        try {
            return Prioridad.valueOf(raw.toUpperCase().trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
