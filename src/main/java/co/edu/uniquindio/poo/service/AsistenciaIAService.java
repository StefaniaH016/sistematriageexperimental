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
                    .sugerenciaRespuesta(
                            jsonRes.optString("sugerenciaRespuesta", sugerenciaBase.getSugerenciaRespuesta()))
                    .justificacionIA(jsonRes.optString("justificacionIA", sugerenciaBase.getJustificacionIA()))
                    .modeloUtilizado("Gemini-1.5-Flash")
                    .generadoEn(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // Fallback: Si falla la IA, devolvemos la sugerencia basada en reglas (RF-11)
            System.err.println("Error llamando a Gemini: " + e.getMessage());
            sugerenciaBase.setJustificacionIA(
                    "Sugerencia basada en reglas de negocio (La conexión con la IA falló). Detalle: " + e.getMessage());
            sugerenciaBase.setModeloUtilizado("REGLAS_NEGOCIO (FALLBACK)");
            sugerenciaBase.setGeneradoEn(LocalDateTime.now());
            return sugerenciaBase;
        }
    }

    private SugerenciaIAResponseDTO aplicarReglasNegocio(SolicitudResponseDTO solicitud) {
        String content = (solicitud.getDescripcion() != null ? solicitud.getDescripcion() : "").toLowerCase();

        String tipoSugerido = TipoSolicitud.CONSULTA_ACADEMICA.name();
        Prioridad prioridadSugerida = Prioridad.MEDIA;

        // --- Reglas de clasificación por palabras clave (RF-02 extendido) ---

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
        // Homologación
        else if (content.contains("homologa") || content.contains("reconocimiento de crédito")
                || content.contains("reconocimiento de credito")) {
            tipoSugerido = TipoSolicitud.HOMOLOGACION.name();
        }
        // Examen supletorio
        else if (content.contains("supletorio") || (content.contains("examen") && content.contains("perdi"))
                || content.contains("examen supletorio")) {
            tipoSugerido = TipoSolicitud.EXAMEN_SUPLETORIO.name();
            prioridadSugerida = Prioridad.ALTA;
        }
        // Cupos
        else if (content.contains("cupo") || content.contains("sobrecupo")) {
            tipoSugerido = TipoSolicitud.SOLICITUD_CUPOS.name();
        }
        // Apoyo económico / becas
        else if (content.contains("apoyo económico") || content.contains("apoyo economico")
                || content.contains("subsidio") || content.contains("dificultad econ")) {
            tipoSugerido = TipoSolicitud.APOYO_ECONOMICO.name();
        } else if (content.contains("beca") || content.contains("descuento") || content.contains("media beca")) {
            tipoSugerido = TipoSolicitud.BECA_DESCUENTO.name();
        }
        // Apoyo psicosocial
        else if (content.contains("psicolog") || content.contains("bienestar") || content.contains("ansiedad")
                || content.contains("estrés") || content.contains("salud mental")) {
            tipoSugerido = TipoSolicitud.APOYO_PSICOSOCIAL.name();
        }
        // Trabajo de grado
        else if (content.contains("trabajo de grado") || content.contains("tesis")
                || content.contains("proyecto de grado")) {
            tipoSugerido = TipoSolicitud.TRABAJO_GRADO.name();
        }
        // Práctica empresarial
        else if (content.contains("práctica") || content.contains("practica") || content.contains("pasantía")
                || content.contains("pasantia")) {
            tipoSugerido = TipoSolicitud.PRACTICA_EMPRESARIAL.name();
        }
        // Certificados y documentos
        else if (content.contains("certificado") || content.contains("constancia")) {
            tipoSugerido = TipoSolicitud.CERTIFICADO_ESTUDIOS.name();
        } else if (content.contains("paz y salvo") || content.contains("paz&salvo")) {
            tipoSugerido = TipoSolicitud.PAZ_Y_SALVO.name();
        } else if (content.contains("acta de grado") || content.contains("grado") || content.contains("graduac")) {
            tipoSugerido = TipoSolicitud.ACTA_GRADO.name();
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
        // Reconocimiento de créditos
        else if (content.contains("reconocimiento") || content.contains("créditos adicionales")) {
            tipoSugerido = TipoSolicitud.RECONOCIMIENTO_CREDITOS.name();
        }

        // Reglas de prioridad por fecha o urgencia (RF-03)
        if (solicitud.getFechaLimite() != null) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), solicitud.getFechaLimite());
            if (dias <= 2)
                prioridadSugerida = Prioridad.CRITICA;
            else if (dias <= 5)
                prioridadSugerida = Prioridad.ALTA;
        }
        if (content.contains("urgente") || content.contains("auxilio") || content.contains("inmediato")
                || content.contains("inminente")) {
            prioridadSugerida = Prioridad.ALTA;
        }

        String descripcionTipo = TipoSolicitud.valueOf(tipoSugerido).getDescripcion();
        return SugerenciaIAResponseDTO.builder()
                .solicitudId(solicitud.getId())
                .tipoSolicitudSugerido(tipoSugerido)
                .prioridadSugerida(prioridadSugerida)
                .resumenHistorial("Solicitud en estado " + solicitud.getEstado() + " con "
                        + (solicitud.getHistorial() != null ? solicitud.getHistorial().size() : 0)
                        + " acciones previas.")
                .sugerenciaRespuesta("Estimado estudiante, hemos recibido su solicitud sobre " + descripcionTipo
                        + ". Estaremos procesándola pronto.")
                .justificacionIA("Sugerencia automática basada en análisis de palabras clave y fechas.")
                .build();
    }

    public SugerenciaIAResponseDTO obtenerClasificacionSugerida(Long solicitudId) {
        return obtenerSugerencia(solicitudId, null);
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
                "- Descripción: " + solicitud.getDescripcion() + "\n" +
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
                "- HOMOLOGACION: Para reconocer materias de otra institución\n" +
                "- RECONOCIMIENTO_CREDITOS: Para reconocer créditos adicionales\n" +
                "- EXAMEN_SUPLETORIO: Para presentar un examen perdido\n" +
                "- MODIFICACION_MATRICULA: Para modificar la matrícula económica o académica\n" +
                "- RESERVA_CUPO: Para reservar un cupo para el siguiente semestre\n" +
                "- REINGRESO: Para volver después de haberse retirado\n" +
                "- TRANSFERENCIA_INTERNA: Para cambiar de programa o facultad\n" +
                "- CERTIFICADO_ESTUDIOS: Para solicitar constancias o certificados\n" +
                "- PAZ_Y_SALVO: Para tramitar paz y salvo académico\n" +
                "- ACTA_GRADO: Para trámites de grado\n" +
                "- APOYO_ECONOMICO: Para solicitar subsidios o apoyo económico\n" +
                "- BECA_DESCUENTO: Para solicitar becas o descuentos en matrícula\n" +
                "- APOYO_PSICOSOCIAL: Para solicitar acompañamiento psicológico\n" +
                "- TRABAJO_GRADO: Para trámites relacionados con trabajo o proyecto de grado\n" +
                "- PRACTICA_EMPRESARIAL: Para trámites de práctica o pasantía\n" +
                "- PROCESO_DISCIPLINARIO: Para apelaciones o procesos disciplinarios\n" +
                "- CONSULTA_ACADEMICA: Para consultas generales que no encajan en otras categorías\n" +
                "\nIMPORTANTE: Si la solicitud menciona algo que no existe textualmente (como 'cancelar semestre'),\n" +
                "debes razonar y elegir la categoría más apropiada (en este caso: CANCELACION_SEMESTRE).\n" +
                "\nREQUISITOS DE SALIDA (JSON):\n" +
                "Debes responder Únicament con un objeto JSON con los siguientes campos:\n" +
                "1. \"tipoSolicitudSugerido\": El nombre exacto del tipo (ej: CANCELACION_SEMESTRE). DEBE ser uno de la lista anterior.\n"
                +
                "2. \"prioridadSugerida\": Nivel de urgencia. DEBE ser exactamente uno de: [CRITICA, ALTA, MEDIA, BAJA].\n"
                +
                "3. \"resumenHistorial\": Un breve resumen (máx 3 frases) del estado actual.\n" +
                "4. \"sugerenciaRespuesta\": Un borrador de respuesta cordial para el estudiante.\n" +
                "5. \"justificacionIA\": Justificación técnica de la clasificación y prioridad.\n" +
                "\nEJEMPLO:\n" +
                "{\n" +
                "  \"tipoSolicitudSugerido\": \"CANCELACION_SEMESTRE\",\n" +
                "  \"prioridadSugerida\": \"ALTA\",\n" +
                "  \"resumenHistorial\": \"La solicitud fue registrada hoy y no tiene acciones previas.\",\n" +
                "  \"sugerenciaRespuesta\": \"Cordial saludo...\",\n" +
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
