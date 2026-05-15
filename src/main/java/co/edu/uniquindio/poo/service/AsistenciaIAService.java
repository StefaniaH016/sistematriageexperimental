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
        
        String prompt = buildPrompt(solicitud, request != null ? request.getContextoAdicional() : null);
        String respuestaIARaw = llamarGeminiAPI(prompt);

        try {
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
            
            return SugerenciaIAResponseDTO.builder()
                    .solicitudId(solicitud.getId())
                    .resumen(jsonRes.optString("resumen", "No se pudo generar un resumen."))
                    .tipoSugerido(jsonRes.optString("tipoSugerido", "CONSULTA_ACADEMICA"))
                    .prioridadSugerida(jsonRes.optString("prioridadSugerida", "MEDIA"))
                    .razonamiento(jsonRes.optString("razonamiento", "Análisis basado en la descripción de la solicitud."))
                    .modeloUtilizado(MODELO)
                    .generadoEn(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // Fallback en caso de que falle el parseo JSON
            return SugerenciaIAResponseDTO.builder()
                    .solicitudId(solicitud.getId())
                    .resumen(respuestaIARaw)
                    .tipoSugerido("CONSULTA_ACADEMICA")
                    .prioridadSugerida("MEDIA")
                    .razonamiento("Error al procesar respuesta estructurada: " + e.getMessage())
                    .modeloUtilizado(MODELO)
                    .generadoEn(LocalDateTime.now())
                    .build();
        }
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
            return "Error al contactar a Gemini: " + e.getMessage();
        }
    }

    private String buildPrompt(SolicitudResponseDTO solicitud, String contexto) {
        return "Actúa como un asistente académico experto de la Universidad. Analiza la siguiente solicitud de un estudiante y genera una respuesta estructurada estrictamente en formato JSON.\n\n" +
               "DETALLES DE LA SOLICITUD:\n" +
               "- Título: " + solicitud.getTitulo() + "\n" +
               "- Descripción: " + solicitud.getDescripcion() + "\n" +
               "- Fecha límite solicitada: " + (solicitud.getFechaLimite() != null ? solicitud.getFechaLimite() : "Ninguna") + "\n" +
               (contexto != null ? "- Contexto adicional del administrativo: " + contexto + "\n" : "") +
               "\nREQUISITOS DE SALIDA:\n" +
               "Debes responder ÚNICAMENTE con un objeto JSON (sin texto adicional fuera del JSON) con los siguientes campos:\n" +
               "1. \"resumen\": Un borrador de respuesta formal, cordial y profesional para el estudiante.\n" +
               "2. \"tipoSugerido\": Clasificación de la solicitud. Debe ser uno de: [REGISTRO_ASIGNATURAS, CANCELACION_ASIGNATURAS, HOMOLOGACION, SOLICITUD_CUPOS, CONSULTA_ACADEMICA].\n" +
               "3. \"prioridadSugerida\": Nivel de urgencia. Debe ser uno de: [ALTA, MEDIA, BAJA].\n" +
               "4. \"razonamiento\": Una breve justificación técnica de por qué elegiste ese tipo y prioridad.\n" +
               "\nEJEMPLO DE FORMATO:\n" +
               "{\n" +
               "  \"resumen\": \"Cordial saludo, hemos recibido su solicitud de cupos...\",\n" +
               "  \"tipoSugerido\": \"SOLICITUD_CUPOS\",\n" +
               "  \"prioridadSugerida\": \"MEDIA\",\n" +
               "  \"razonamiento\": \"La solicitud trata sobre falta de cupos en asignaturas de cuarto semestre.\"\n" +
               "}";
    }
}
