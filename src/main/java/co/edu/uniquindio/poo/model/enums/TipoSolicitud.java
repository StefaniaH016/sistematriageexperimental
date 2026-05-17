package co.edu.uniquindio.poo.model.enums;

/**
 * Tipos de solicitud académica disponibles en el sistema.
 * RF-02: Clasificación de solicitudes según su tipo.
 */
public enum TipoSolicitud {

    // Trámites de asignaturas
    REGISTRO_ASIGNATURAS("Registro de asignaturas"),
    CANCELACION_ASIGNATURAS("Cancelación de asignaturas"),
    SOLICITUD_CUPOS("Solicitud de cupos"),
    HOMOLOGACION("Homologación"),
    EXAMEN_SUPLETORIO("Examen supletorio"),
    HABILITACION("Examen de habilitación"),

    // Trámites de matrícula y continuidad
    MODIFICACION_MATRICULA("Modificación de matrícula"),
    CANCELACION_SEMESTRE("Cancelación de semestre"),
    RESERVA_CUPO("Reserva de cupo"),
    REINGRESO("Reingreso"),
    TRANSFERENCIA_INTERNA("Transferencia interna de programa"),

    // Trámites documentales y certificaciones
    CERTIFICADO_ESTUDIOS("Certificado de estudios"),
    PAZ_Y_SALVO("Paz y salvo académico"),
    ACTA_GRADO("Trámite de acta de grado"),

    // Apoyos y bienestar
    APOYO_ECONOMICO("Solicitud de apoyo económico"),
    BECA_DESCUENTO("Solicitud de beca o descuento"),
    APOYO_PSICOSOCIAL("Solicitud de apoyo psicosocial"),

    // Procesos académicos especiales
    RECONOCIMIENTO_CREDITOS("Reconocimiento de créditos"),
    TRABAJO_GRADO("Trámite de trabajo de grado"),
    PRACTICA_EMPRESARIAL("Trámite de práctica empresarial"),
    PROCESO_DISCIPLINARIO("Proceso disciplinario o apelación"),

    // General
    CONSULTA_ACADEMICA("Consulta académica");

    private final String descripcion;

    TipoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
