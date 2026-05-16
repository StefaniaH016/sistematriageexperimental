package co.edu.uniquindio.poo.model.enums;

/**
 * Estados posibles dentro del ciclo de vida de una solicitud.
 * RF-04: Registrada → Clasificada → En atención → Atendida → Cerrada.
 * Se valida que las transiciones entre estados sean coherentes.
 */
public enum EstadoSolicitud {

    REGISTRADA("Registrada"),
    CLASIFICADA("Clasificada"),
    EN_ATENCION("En atención"),
    ATENDIDA("Atendida"),
    CERRADA("Cerrada");

    private final String descripcion;

    EstadoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Valida si la transición del estado actual al estado destino es válida.
     * Transiciones permitidas:
     * REGISTRADA → CLASIFICADA
     * CLASIFICADA → EN_ATENCION
     * EN_ATENCION → ATENDIDA
     * ATENDIDA → CERRADA
     *
     * @param destino Estado al que se desea transicionar.
     * @return true si la transición es válida, false en caso contrario.
     */
    public boolean puedeTransicionarA(EstadoSolicitud destino) {
        return switch (this) {
            case REGISTRADA -> destino == CLASIFICADA;
            // RF-02: Permite reclasificación (corregir tipo o aplicar sugerencia de IA)
            case CLASIFICADA -> destino == EN_ATENCION || destino == CLASIFICADA;
            case EN_ATENCION -> destino == ATENDIDA;
            case ATENDIDA -> destino == CERRADA;
            case CERRADA -> false; // Estado final, no permite transiciones
        };
    }
}
