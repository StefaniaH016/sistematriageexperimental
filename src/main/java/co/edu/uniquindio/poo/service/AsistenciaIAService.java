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

    private static final String MODELO = "gemini-1.5-flash";

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
            String tipoSugeridoRaw = jsonRes.optString("tipoSolicitudSugerido", sugerenciaBase.getTipoSolicitudSugerido());
            String prioridadRaw = jsonRes.optString("prioridadSugerida", sugerenciaBase.getPrioridadSugerida().name());
            
            return SugerenciaIAResponseDTO.builder()
                    .solicitudId(solicitudId)
                    .tipoSolicitudSugerido(tipoSugeridoRaw)
                    .prioridadSugerida(parsePrioridad(prioridadRaw, sugerenciaBase.getPrioridadSugerida()))
                    .resumenHistorial(jsonRes.optString("resumenHistorial", sugerenciaBase.getResumenHistorial()))
                    .sugerenciaRespuesta(jsonRes.optString("sugerenciaRespuesta", sugerenciaBase.getSugerenciaRespuesta()))
                    .justificacionIA(jsonRes.optString("justificacionIA", sugerenciaBase.getJustificacionIA()))
                    .modeloUtilizado("Gemini-1.5-Flash")
                    .generadoEn(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // Fallback: Si falla la IA, devolvemos la sugerencia basada en reglas (RF-11)
            System.err.println("Error llamando a Gemini: " + e.getMessage());
            sugerenciaBase.setJustificacionIA("Sugerencia basada en reglas de negocio (La conexión con la IA falló). Detalle: " + e.getMessage());
            sugerenciaBase.setModeloUtilizado("REGLAS_NEGOCIO (FALLBACK)");
            sugerenciaBase.setGeneradoEn(LocalDateTime.now());
            return sugerenciaBase;
        }
    }

    private SugerenciaIAResponseDTO aplicarReglasNegocio(SolicitudResponseDTO solicitud) {
        String content = (solicitud.getDescripcion() != null ? solicitud.getDescripcion() : "").toLowerCase();
        
        String tipoSugerido = "Consulta académica";
        Prioridad prioridadSugerida = Prioridad.MEDIA;

        // Reglas de clasificación por palabras clave (RF-02 extendido)
        if (content.contains("registro") || content.contains("materia") || content.contains("asignatura")) {
            tipoSugerido = "Registro de asignaturas";
        } else if (content.contains("homologa") || content.contains("reconocimiento")) {
            tipoSugerido = "Homologación";
        } else if (content.contains("cancel") || content.contains("retira")) {
            tipoSugerido = "Cancelación de asignaturas";
        } else if (content.contains("cupo") || content.contains("sobrecupo")) {
            tipoSugerido = "Solicitud de cupos";
        } else if (content.contains("supletorio") || content.contains("examen") || content.contains("parcial")) {
            tipoSugerido = "Examen supletorio";
            prioridadSugerida = Prioridad.ALTA;
        } else if (content.contains("certificado") || content.contains("constancia")) {
            tipoSugerido = "Certificado de estudios";
        } else if (content.contains("reserva")) {
            tipoSugerido = "Reserva de cupo";
        } else if (content.contains("reingreso")) {
            tipoSugerido = "Reingreso";
        } else if (content.contains("modifica") && (content.contains("matrícula") || content.contains("matricula"))) {
            tipoSugerido = "Modificación de matrícula";
        }

        // Reglas de prioridad por fecha o urgencia (RF-03)
        if (solicitud.getFechaLimite() != null) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), solicitud.getFechaLimite());
            if (dias <= 2) prioridadSugerida = Prioridad.CRITICA;
            else if (dias <= 5) prioridadSugerida = Prioridad.ALTA;
        }

        if (content.contains("urgente") || content.contains("auxilio") || content.contains("inmediato")) {
            prioridadSugerida = Prioridad.ALTA;
        }

        return SugerenciaIAResponseDTO.builder()
                .solicitudId(solicitud.getId())
                .tipoSolicitudSugerido(tipoSugerido)
                .prioridadSugerida(prioridadSugerida)
                .resumenHistorial("Solicitud en estado " + solicitud.getEstado() + " con " + (solicitud.getHistorial() != null ? solicitud.getHistorial().size() : 0) + " acciones previas.")
                .sugerenciaRespuesta("Estimado estudiante, hemos recibido su solicitud sobre " + tipoSugerido + ". Estaremos procesándola pronto.")
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
                historialStr.append("- [").append(h.getFechaHora()).append("] ").append(h.getAccion()).append(": ").append(h.getObservaciones()).append("\n");
            }
        }

        return "Actúa como un asistente académico experto de la Universidad. Analiza la siguiente solicitud de un estudiante y genera una respuesta estructurada estrictamente en formato JSON.\n\n" +
               "DETALLES DE LA SOLICITUD:\n" +
               "- Título/Asunto: (Inferido del contexto)\n" +
               "- Descripción: " + solicitud.getDescripcion() + "\n" +
               "- Fecha límite asociada: " + (solicitud.getFechaLimite() != null ? solicitud.getFechaLimite() : "Ninguna") + "\n" +
               historialStr.toString() +
               (contexto != null ? "- Contexto adicional del administrativo: " + contexto + "\n" : "") +
               "\nREQUISITOS DE SALIDA (JSON):\n" +
               "Debes responder ÚNICAMENTE con un objeto JSON con los siguientes campos:\n" +
               "1. \"tipoSolicitudSugerido\": Clasificación libre de la solicitud (ej: Registro, Cancelación, Supletorio, Homologación, Cupos, etc.).\n" +
               "2. \"prioridadSugerida\": Nivel de urgencia. DEBE ser exactamente uno de estos valores: [CRITICA, ALTA, MEDIA, BAJA]. No uses otros términos.\n" +
               "3. \"resumenHistorial\": Un breve resumen (máx 3 frases) del estado actual basado en el historial.\n" +
               "4. \"sugerenciaRespuesta\": Un borrador de respuesta cordial para el estudiante.\n" +
               "5. \"justificacionIA\": Justificación técnica de la clasificación y prioridad.\n" +
               "\nEJEMPLO:\n" +
               "{\n" +
               "  \"tipoSolicitudSugerido\": \"Supletorio de Examen\",\n" +
               "  \"prioridadSugerida\": \"ALTA\",\n" +
               "  \"resumenHistorial\": \"La solicitud fue registrada hoy y no tiene acciones previas.\",\n" +
               "  \"sugerenciaRespuesta\": \"Cordial saludo...\",\n" +
               "  \"justificacionIA\": \"El estudiante solicita un supletorio con fecha próxima.\"\n" +
               "}";
    }
    private Prioridad parsePrioridad(String raw, Prioridad fallback) {
        if (raw == null || raw.isEmpty()) return fallback;
        try {
            return Prioridad.valueOf(raw.toUpperCase().trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
