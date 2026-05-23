package co.edu.uniquindio.poo.model.entity;

import co.edu.uniquindio.poo.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad principal que representa una solicitud académica.
 * 
 * RF-01: Almacena tipo, descripción, canal de origen, fecha/hora y solicitante.
 * RF-02: Clasificación según tipo de solicitud.
 * RF-03: Priorización con justificación.
 * RF-04: Gestión del ciclo de vida (Registrada → Clasificada → En atención → Atendida → Cerrada).
 * RF-05: Asignación de responsable.
 * RF-08: Cierre con observación, solicitud cerrada no se modifica.
 */
@Entity
@Table(name = "solicitudes", indexes = {
    @Index(name = "idx_solicitud_estado", columnList = "estado"),
    @Index(name = "idx_solicitud_responsable", columnList = "responsable_id"),
    @Index(name = "idx_solicitud_solicitante", columnList = "solicitante_id"),
    @Index(name = "idx_solicitud_fecha_registro", columnList = "fechaRegistro")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Título descriptivo de la solicitud */
    @Column(length = 200)
    private String titulo;

    /** RF-02: Tipo de solicitud (Registro, Homologación, Cancelación, Supletorio, etc.) */
    @Column(length = 100)
    private String tipoSolicitud;

    /** RF-01: Descripción detallada de la solicitud */
    @Column(nullable = false, length = 2000)
    private String descripcion;

    /** RF-01: Canal por el que ingresó la solicitud */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalOrigen canalOrigen;

    /** RF-01: Fecha y hora de registro automática */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** RF-01: Usuario que realiza la solicitud */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    /** RF-04: Estado actual dentro del ciclo de vida */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    /** RF-03: Prioridad asignada según reglas de negocio */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Prioridad prioridad;

    /** RF-03: Justificación de la prioridad asignada */
    @Column(length = 500)
    private String justificacionPrioridad;

    /** RF-05: Responsable asignado para atender la solicitud */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    /** RF-03: Fecha límite asociada (factor de priorización) */
    private LocalDate fechaLimite;

    /** RF-08: Observación registrada al cerrar la solicitud */
    @Column(length = 1000)
    private String observacionCierre;

    /** RF-06: Historial auditable de acciones sobre la solicitud */
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC")
    @Builder.Default
    private List<HistorialSolicitud> historial = new ArrayList<>();

    /**
     * Establece valores por defecto antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoSolicitud.REGISTRADA;
        }
    }

    /**
     * Agrega una entrada al historial de la solicitud.
     */
    public void agregarHistorial(HistorialSolicitud entrada) {
        historial.add(entrada);
        entrada.setSolicitud(this);
    }

    /**
     * RF-08: Valida inmutabilidad de la solicitud
     */
    private void validarMutabilidad() {
        if (this.estado == EstadoSolicitud.CERRADA) {
            throw new co.edu.uniquindio.poo.exception.OperacionNoPermitidaException("La solicitud está CERRADA y es inmutable (RF-08).");
        }
    }

    /**
     * RF-02: Clasifica la solicitud asignando el tipo y cambiando el estado.
     * La prioridad se asigna en un paso separado (RF-03).
     */
    public void clasificar(String tipoSolicitud) {
        validarMutabilidad();
        this.tipoSolicitud = tipoSolicitud;
        this.estado = EstadoSolicitud.CLASIFICADA;
    }

    /**
     * RF-03: Ajusta manualmente la prioridad y su justificación.
     */
    public void asignarPrioridad(Prioridad prioridad, String justificacion) {
        validarMutabilidad();
        this.prioridad = prioridad;
        this.justificacionPrioridad = justificacion;
    }

    /**
     * RF-04: Actualiza el estado dentro del ciclo de vida.
     */
    public void transicionarEstado(EstadoSolicitud nuevoEstado) {
        validarMutabilidad();
        this.estado = nuevoEstado;
    }

    /**
     * RF-05: Asigna un responsable a la solicitud.
     */
    public void asignarResponsable(Usuario responsable) {
        validarMutabilidad();
        this.responsable = responsable;
    }

    /**
     * RF-08: Cierra formalmente la solicitud con observación.
     */
    public void cerrar(String observacionCierre) {
        validarMutabilidad();
        this.estado = EstadoSolicitud.CERRADA;
        this.observacionCierre = observacionCierre;
    }
}
