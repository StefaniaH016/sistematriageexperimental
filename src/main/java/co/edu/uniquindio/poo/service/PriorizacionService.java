package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.model.entity.Solicitud;
import co.edu.uniquindio.poo.model.enums.Prioridad;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Servicio encargado de calcular la prioridad de una solicitud basándose en
 * reglas de negocio.
 * RF-03: Priorización basada en tipo de solicitud, impacto académico y fecha
 * límite.
 *
 * Motor de reglas:
 * 1. Tipo de solicitud: Registro/Cancelación de asignaturas → mayor urgencia.
 * 2. Fecha límite: Mientras más cercana, mayor prioridad.
 * 3. Impacto académico: Solicitudes que afectan matrícula tienen mayor peso.
 */
@Service
public class PriorizacionService {

    /**
     * Calcula automáticamente la prioridad de una solicitud y genera la
     * justificación.
     * Retorna un arreglo: [0] = Prioridad, [1] = Justificación (String).
     */
    public Object[] calcularPrioridad(Solicitud solicitud) {
        int puntaje = 0;
        String razonTipo = "";
        String razonFecha = "";
        String razonDescripcion = "";

        // Regla 1: Prioridad por tipo de solicitud
        String[] resultadoTipo = calcularPuntajePorTipo(solicitud.getTipoSolicitud());
        puntaje += Integer.parseInt(resultadoTipo[0]);
        razonTipo = resultadoTipo[1];

        // Regla 2: Prioridad por fecha límite
        String[] resultadoFecha = calcularPuntajePorFechaLimite(solicitud.getFechaLimite());
        puntaje += Integer.parseInt(resultadoFecha[0]);
        razonFecha = resultadoFecha[1];

        // Regla 3: Señales semánticas de urgencia en la descripción
        String[] resultadoDesc = calcularPuntajePorDescripcion(solicitud.getDescripcion());
        puntaje += Integer.parseInt(resultadoDesc[0]);
        razonDescripcion = resultadoDesc[1];

        // Determinar prioridad según puntaje total
        Prioridad prioridad = determinarPrioridad(puntaje);

        // Construir justificación narrativa clara para el usuario
        String justificacion = construirJustificacionNarrativa(prioridad, razonTipo, razonFecha, razonDescripcion);

        return new Object[] { prioridad, justificacion };
    }

    /**
     * Construye una justificación narrativa y legible para el usuario final,
     * explicando el razonamiento detrás de la prioridad asignada.
     */
    private String construirJustificacionNarrativa(Prioridad prioridad, String razonTipo,
            String razonFecha, String razonDescripcion) {
        StringBuilder sb = new StringBuilder();
        sb.append("Se asignó prioridad ").append(prioridad.getDescripcion()).append(" porque: ");
        sb.append(razonTipo);
        if (!razonFecha.isEmpty()) {
            sb.append(" Adicionalmente, ").append(razonFecha.toLowerCase());
        }
        if (!razonDescripcion.isEmpty()) {
            sb.append(" ").append(razonDescripcion);
        }
        return sb.toString().trim();
    }

    /**
     * Calcula el puntaje basado en el tipo de solicitud.
     * Retorna [puntaje, razón narrativa].
     */
    private String[] calcularPuntajePorTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return new String[]{"0", ""};
        }

        String t = tipo.toLowerCase();

        if (t.contains("cancelacion_semestre") || t.contains("cancelacion de semestre")
                || t.contains("cancelar semestre")) {
            return new String[]{"5", "La cancelación de semestre tiene un impacto crítico en la trayectoria académica del estudiante y requiere atención inmediata."};
        }
        if (t.contains("registro") || t.contains("cancelacion_asignaturas") || t.contains("cancelación de asignaturas")
                || t.contains("materia") || t.contains("asignatura")) {
            return new String[]{"4", "Los trámites relacionados con asignaturas afectan directamente la matrícula activa del estudiante."};
        }
        if (t.contains("supletorio") || t.contains("examen")) {
            return new String[]{"4", "Los exámenes supletorios o de habilitación tienen fechas estrictas que no pueden postergarse sin consecuencias académicas."};
        }
        if (t.contains("modificacion_matricula") || t.contains("modificación de matrícula")
                || t.contains("modificación") && t.contains("matrícula")) {
            return new String[]{"4", "La modificación de matrícula impacta el plan académico del estudiante y debe atenderse durante el período habilitado."};
        }
        if (t.contains("disciplinario") || t.contains("apelacion") || t.contains("apelación")) {
            return new String[]{"4", "Los procesos disciplinarios o de apelación tienen implicaciones legales y académicas que demandan atención urgente."};
        }
        if (t.contains("trabajo_grado") || t.contains("trabajo de grado") || t.contains("practica_empresarial")
                || t.contains("práctica")) {
            return new String[]{"3", "Los trámites de trabajo de grado o práctica están vinculados a la etapa final de la carrera, lo que les otorga un impacto significativo."};
        }
        if (t.contains("homologa") || t.contains("reconocimiento") || t.contains("reconocimiento_creditos")) {
            return new String[]{"3", "La homologación y reconocimiento de créditos influye en el plan de estudios y el avance académico del estudiante."};
        }
        if (t.contains("cupo") || t.contains("sobrecupo") || t.contains("reserva") || t.contains("reingreso")) {
            return new String[]{"3", "La solicitud de cupos o reingreso puede afectar la continuidad académica del estudiante si no se atiende oportunamente."};
        }
        if (t.contains("apoyo_economico") || t.contains("apoyo económico") || t.contains("beca")
                || t.contains("descuento")) {
            return new String[]{"3", "El apoyo económico incide directamente en la capacidad del estudiante para continuar sus estudios."};
        }
        if (t.contains("transferencia")) {
            return new String[]{"2", "La transferencia interna implica un cambio de programa académico que puede tener consecuencias en el historial y créditos del estudiante."};
        }
        if (t.contains("psicosocial") || t.contains("psicológico")) {
            return new String[]{"2", "El acompañamiento psicosocial requiere atención personalizada, aunque no suele tener plazos académicos críticos inmediatos."};
        }
        if (t.contains("certificado") || t.contains("paz_y_salvo") || t.contains("paz y salvo")
                || t.contains("acta_grado") || t.contains("acta de grado")) {
            return new String[]{"1", "Los trámites documentales como certificados o constancias tienen bajo impacto en el proceso académico activo del estudiante."};
        }

        return new String[]{"1", "La consulta o trámite general no presenta indicadores de urgencia académica inmediata."};
    }

    /**
     * Calcula el puntaje basado en la cercanía de la fecha límite.
     * Retorna [puntaje, razón narrativa].
     */
    private String[] calcularPuntajePorFechaLimite(LocalDate fechaLimite) {
        if (fechaLimite == null) {
            return new String[]{"0", ""};
        }

        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);

        if (diasRestantes < 0) {
            return new String[]{"5", "La fecha límite ya venció, lo que hace urgente una resolución inmediata."};
        } else if (diasRestantes <= 2) {
            return new String[]{"4", "La fecha límite vence en menos de 2 días, lo que impone máxima urgencia en la atención."};
        } else if (diasRestantes <= 5) {
            return new String[]{"3", "La fecha límite es en menos de 5 días, requiriendo atención preferente en el corto plazo."};
        } else if (diasRestantes <= 10) {
            return new String[]{"2", "La fecha límite está próxima (menos de 10 días), lo cual incrementa la urgencia de atención."};
        } else {
            return new String[]{"1", "La fecha límite es lejana, lo que permite un margen razonable de atención."};
        }
    }

    /**
     * Calcula puntaje adicional por señales de urgencia presentes en la descripción.
     * Retorna [puntaje, razón narrativa].
     */
    private String[] calcularPuntajePorDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return new String[]{"0", ""};
        }

        String texto = descripcion.toLowerCase();
        int puntaje = 0;

        if (texto.contains("urgente") || texto.contains("inminente")) {
            puntaje += 2;
        }
        if (texto.contains("matricula") || texto.contains("cierre")) {
            puntaje += 1;
        }

        if (puntaje > 0) {
            return new String[]{String.valueOf(puntaje), "La descripción de la solicitud contiene términos de urgencia que elevan la prioridad de atención."};
        }
        return new String[]{"0", ""};
    }

    /**
     * Determina la prioridad final según el puntaje acumulado.
     */
    private Prioridad determinarPrioridad(int puntaje) {
        if (puntaje >= 7)
            return Prioridad.CRITICA;
        if (puntaje >= 5)
            return Prioridad.ALTA;
        if (puntaje >= 3)
            return Prioridad.MEDIA;
        return Prioridad.BAJA;
    }
}
