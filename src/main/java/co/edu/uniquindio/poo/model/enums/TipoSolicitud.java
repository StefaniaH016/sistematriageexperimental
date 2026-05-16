package co.edu.uniquindio.poo.model.enums;

/**
 * Tipos de solicitud académica disponibles en el sistema.
 * RF-02: Clasificación de solicitudes según su tipo.
 */
public enum TipoSolicitud {

    REGISTRO_ASIGNATURAS("Registro de asignaturas"),
    HOMOLOGACION("Homologación"),
    CANCELACION_ASIGNATURAS("Cancelación de asignaturas"),
    SOLICITUD_CUPOS("Solicitud de cupos"),
    EXAMEN_SUPLETORIO("Examen supletorio"),
    CERTIFICADO_ESTUDIOS("Certificado de estudios"),
    MODIFICACION_MATRICULA("Modificación de matrícula"),
    RESERVA_CUPO("Reserva de cupo"),
    REINGRESO("Reingreso"),
    CONSULTA_ACADEMICA("Consulta académica");

    private final String descripcion;

    TipoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
