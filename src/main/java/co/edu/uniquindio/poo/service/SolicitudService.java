package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.dto.solicitud.*;
import co.edu.uniquindio.poo.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.poo.dto.common.PageResponseDTO;
import co.edu.uniquindio.poo.dto.solicitud.SolicitudResponseDTO;
import co.edu.uniquindio.poo.exception.*;
import co.edu.uniquindio.poo.mapper.EntityMapper;
import co.edu.uniquindio.poo.model.entity.HistorialSolicitud;
import co.edu.uniquindio.poo.model.entity.Solicitud;
import co.edu.uniquindio.poo.model.entity.Usuario;
import co.edu.uniquindio.poo.model.enums.*;
import co.edu.uniquindio.poo.repository.HistorialSolicitudRepository;
import co.edu.uniquindio.poo.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio principal para la gestión del ciclo de vida completo de las
 * solicitudes académicas.
 * 
 * Implementa los siguientes requisitos funcionales:
 * RF-01: Registro de solicitudes académicas
 * RF-02: Clasificación de solicitudes
 * RF-03: Priorización de solicitudes
 * RF-04: Gestión del ciclo de vida (transiciones de estado)
 * RF-05: Asignación de responsables
 * RF-06: Registro del historial auditable
 * RF-07: Consulta de solicitudes con filtros
 * RF-08: Cierre de solicitudes
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final UsuarioService usuarioService;
    private final PriorizacionService priorizacionService;
    private final ActorContextService actorContextService;
    private final EntityMapper mapper;

    // ==================== RF-01: REGISTRO DE SOLICITUDES ====================

    /**
     * Registra una nueva solicitud académica en el sistema.
     * RF-01: Almacena descripción, canal de origen, fecha/hora y solicitante.
     * El estado inicial es REGISTRADA y se genera la primera entrada en el
     * historial.
     */
    public SolicitudResponseDTO registrarSolicitud(SolicitudRequestDTO request) {

        Usuario actorActual = actorContextService.obtenerActorActual();
        Usuario solicitante;

        if (request.getSolicitanteId() != null && !request.getSolicitanteId().equals(actorActual.getId())) {
            if (actorActual.getRol() == co.edu.uniquindio.poo.model.enums.Rol.ADMINISTRATIVO) {
                solicitante = usuarioService.buscarUsuarioPorId(request.getSolicitanteId());
            } else {
                throw new co.edu.uniquindio.poo.exception.OperacionNoPermitidaException(
                        "No tiene permisos para registrar una solicitud a nombre de otro usuario.");
            }
        } else {
            solicitante = actorActual;
        }

        if (request.getFechaLimite() != null && request.getFechaLimite().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha límite no puede ser anterior a la fecha actual");
        }

        // Crear la solicitud con estado inicial REGISTRADA
        Solicitud solicitud = Solicitud.builder()
                .descripcion(request.getDescripcion())
                .canalOrigen(request.getCanalOrigen())
                .fechaRegistro(LocalDateTime.now())
                .solicitante(solicitante)
                .estado(EstadoSolicitud.REGISTRADA)
                .fechaLimite(request.getFechaLimite())
                .build();

        // RF-06: Registrar acción en el historial
        HistorialSolicitud historial = crearEntradaHistorial(
                solicitud, solicitante,
                "Solicitud registrada",
                "Solicitud creada a través del canal: " + request.getCanalOrigen().getDescripcion());
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return mapper.toSolicitudDTO(solicitud);
    }

    // ==================== RF-02: CLASIFICACIÓN DE SOLICITUDES ====================

    /**
     * Clasifica una solicitud según su tipo y cambia su estado a CLASIFICADA.
     * RF-02: Clasificación según tipo (Registro, Homologación, Cancelación, etc.)
     * RF-04: Transición REGISTRADA → CLASIFICADA
     */
    public SolicitudResponseDTO clasificarSolicitud(Long solicitudId, ClasificacionRequestDTO request) {
        Solicitud solicitud = buscarSolicitudPorId(solicitudId);
        Usuario usuario = actorContextService.obtenerActorActual();

        // RF-13: Autorización
        if (usuario.getRol() != Rol.ADMINISTRATIVO) {
            throw new OperacionNoPermitidaException("Solo los usuarios administrativos pueden clasificar solicitudes.");
        }

        // RF-02: Validar que el tipo de solicitud no sea nulo ni vacío
        if (request.getTipoSolicitud() == null || request.getTipoSolicitud().trim().isEmpty()) {
            throw new co.edu.uniquindio.poo.exception.OperacionNoPermitidaException(
                    "El tipo de solicitud es obligatorio para realizar la clasificación (RF-02).");
        }

        // RF-08: Verificar que no esté cerrada
        validarNoEstaCerrada(solicitud);

        // RF-04: Validar transición de estado
        validarTransicion(solicitud, EstadoSolicitud.CLASIFICADA);

        // RF-02: Establecer tipo para calcular prioridad sobre la solicitud clasificada
        solicitud.setTipoSolicitud(request.getTipoSolicitud());

        // RF-03: Calcular prioridad automáticamente basada en reglas
        Object[] resultado = priorizacionService.calcularPrioridad(solicitud);
        solicitud.clasificar(
                request.getTipoSolicitud(),
                (Prioridad) resultado[0],
                (String) resultado[1]);

        // RF-06: Registrar en historial
        HistorialSolicitud historial = crearEntradaHistorial(
                solicitud, usuario,
                "Solicitud clasificada como: " + request.getTipoSolicitud(),
                request.getObservaciones() != null ? request.getObservaciones()
                        : "Prioridad asignada: " + solicitud.getPrioridad().getDescripcion());
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return mapper.toSolicitudDTO(solicitud);
    }

    // ==================== RF-03: PRIORIZACIÓN MANUAL ====================

    /**
     * Permite ajustar manualmente la prioridad de una solicitud con justificación.
     * RF-03: Priorización con justificación obligatoria.
     * RF-10: La sugerencia de la IA puede ser ajustada por un humano.
     */
    public SolicitudResponseDTO priorizarSolicitud(Long solicitudId, PriorizacionRequestDTO request) {
        Solicitud solicitud = buscarSolicitudPorId(solicitudId);
        Usuario usuario = actorContextService.obtenerActorActual();

        // RF-13: Autorización
        if (usuario.getRol() != Rol.ADMINISTRATIVO) {
            throw new OperacionNoPermitidaException("Solo los usuarios administrativos pueden priorizar solicitudes manualmente.");
        }

        validarNoEstaCerrada(solicitud);

        if (request.getPrioridad() == null) {
            throw new co.edu.uniquindio.poo.exception.OperacionNoPermitidaException("La prioridad es obligatoria.");
        }

        if (request.getJustificacion() == null || request.getJustificacion().trim().isEmpty()) {
            throw new co.edu.uniquindio.poo.exception.OperacionNoPermitidaException(
                    "La justificación es obligatoria para cambios manuales de prioridad (RF-03).");
        }

        Prioridad prioridadAnterior = solicitud.getPrioridad();
        solicitud.asignarPrioridad(request.getPrioridad(), request.getJustificacion());

        // RF-06: Registrar en historial
        String accion = prioridadAnterior != null
                ? "Prioridad cambiada de " + prioridadAnterior.getDescripcion() + " a "
                        + request.getPrioridad().getDescripcion()
                : "Prioridad asignada: " + request.getPrioridad().getDescripcion();

        HistorialSolicitud historial = crearEntradaHistorial(
                solicitud, usuario, accion, request.getJustificacion());
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return mapper.toSolicitudDTO(solicitud);
    }

    // ==================== RF-04: CAMBIO DE ESTADO ====================

    /**
     * Cambia el estado de una solicitud validando las transiciones permitidas.
     * RF-04: Registrada → Clasificada → En atención → Atendida → Cerrada
     */
    public SolicitudResponseDTO cambiarEstado(Long solicitudId, CambioEstadoRequestDTO request) {
        Solicitud solicitud = buscarSolicitudPorId(solicitudId);
        Usuario usuario = actorContextService.obtenerActorActual();

        validarNoEstaCerrada(solicitud);
        validarTransicion(solicitud, request.getNuevoEstado());

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.transicionarEstado(request.getNuevoEstado());

        // RF-06: Registrar en historial
        HistorialSolicitud historial = crearEntradaHistorial(
                solicitud, usuario,
                "Estado cambiado de " + estadoAnterior.getDescripcion() + " a "
                        + request.getNuevoEstado().getDescripcion(),
                request.getObservaciones());
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return mapper.toSolicitudDTO(solicitud);
    }

    // ==================== RF-05: ASIGNACIÓN DE RESPONSABLES ====================

    /**
     * Asigna un responsable a una solicitud.
     * RF-05: El responsable debe estar activo y la asignación queda registrada.
     */
    public SolicitudResponseDTO asignarResponsable(Long solicitudId, AsignacionRequestDTO request) {
        Solicitud solicitud = buscarSolicitudPorId(solicitudId);
        Usuario responsable = usuarioService.buscarUsuarioPorId(request.getResponsableId());
        Usuario asignador = actorContextService.obtenerActorActual();

        // RF-13: Autorización
        if (asignador.getRol() != Rol.ADMINISTRATIVO && asignador.getRol() != Rol.RESPONSABLE) {
            throw new OperacionNoPermitidaException("Rol no autorizado para asignar responsables.");
        }
        
        // Un responsable solo puede asignarse la solicitud a sí mismo
        if (asignador.getRol() == Rol.RESPONSABLE && !asignador.getId().equals(request.getResponsableId())) {
            throw new OperacionNoPermitidaException("Un responsable solo puede autoasignarse solicitudes.");
        }
        
        // Regla de Negocio: Un responsable no puede tomar solicitudes sin clasificar
        if (asignador.getRol() == Rol.RESPONSABLE && solicitud.getEstado() == EstadoSolicitud.REGISTRADA) {
            throw new OperacionNoPermitidaException("Un responsable no puede tomar una solicitud que aún no ha sido clasificada por un administrador.");
        }

        validarNoEstaCerrada(solicitud);

        // RF-05: Verificar que el responsable esté activo
        if (!responsable.getActivo()) {
            throw new OperacionNoPermitidaException(
                    "El responsable " + responsable.getNombreCompleto() + " no está activo en el sistema");
        }

        // Verificar que tenga rol apropiado para ser responsable
        if (responsable.getRol() != Rol.RESPONSABLE && responsable.getRol() != Rol.ADMINISTRATIVO) {
            throw new OperacionNoPermitidaException(
                    "El usuario no tiene un rol válido para ser responsable de una solicitud");
        }

        solicitud.asignarResponsable(responsable);

        // RF-06: Registrar asignación en historial
        HistorialSolicitud historial = crearEntradaHistorial(
                solicitud, asignador,
                "Responsable asignado: " + responsable.getNombreCompleto(),
                request.getObservaciones());
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return mapper.toSolicitudDTO(solicitud);
    }

    // ==================== RF-08: CIERRE DE SOLICITUDES ====================

    /**
     * Cierra una solicitud.
     * RF-08: Solo se puede cerrar si está ATENDIDA y se requiere observación de
     * cierre.
     * Una vez cerrada, no puede ser modificada.
     */
    public SolicitudResponseDTO cerrarSolicitud(Long solicitudId, CierreRequestDTO request) {
        Solicitud solicitud = buscarSolicitudPorId(solicitudId);
        Usuario administrativo = actorContextService.obtenerActorActual();

        // RF-13: Autorización
        if (administrativo.getRol() != Rol.ADMINISTRATIVO) {
            throw new OperacionNoPermitidaException("Solo los usuarios administrativos pueden cerrar solicitudes.");
        }

        if (request.getObservacionCierre() == null || request.getObservacionCierre().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La observación de cierre es obligatoria (RF-08).");
        }

        // RF-08: Verificar que la solicitud esté en estado ATENDIDA
        if (solicitud.getEstado() != EstadoSolicitud.ATENDIDA) {
            throw new co.edu.uniquindio.poo.exception.TransicionInvalidaException(
                    "Solo se pueden cerrar solicitudes en estado ATENDIDA.");
        }

        // Aplicar cierre
        solicitud.cerrar(request.getObservacionCierre());

        // RF-06: Registrar cierre en historial
        HistorialSolicitud historial = crearEntradaHistorial(
                solicitud, administrativo,
                "Solicitud cerrada",
                request.getObservacionCierre());
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return mapper.toSolicitudDTO(solicitud);
    }

    // ==================== ELIMINACIÓN ====================

    /**
     * Elimina físicamente una solicitud y su historial de la base de datos.
     * Solo permitido si está en estado CERRADA.
     */
    public void eliminarSolicitud(Long solicitudId) {
        Solicitud solicitud = buscarSolicitudPorId(solicitudId);
        Usuario usuario = actorContextService.obtenerActorActual();

        if (solicitud.getEstado() != EstadoSolicitud.CERRADA) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden eliminar solicitudes que se encuentren en estado CERRADA.");
        }

        if (usuario.getRol() != Rol.ADMINISTRATIVO) {
            throw new OperacionNoPermitidaException(
                    "Solo los usuarios administrativos pueden eliminar solicitudes.");
        }

        solicitudRepository.delete(solicitud);
    }

    // ==================== RF-07: CONSULTA DE SOLICITUDES ====================

    /**
     * Obtiene una solicitud por su ID con toda su información.
     */
    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerPorId(Long id) {
        Solicitud solicitud = buscarSolicitudPorId(id);
        return mapper.toSolicitudDTO(solicitud);
    }

    /**
     * Obtiene todas las solicitudes registradas.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> obtenerTodas() {
        return mapper.toSolicitudDTOList(solicitudRepository.findAll());
    }

    /**
     * RF-07: Consulta solicitudes según criterios: estado, tipo, prioridad,
     * responsable.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> consultarConFiltros(
            EstadoSolicitud estado,
            String tipo,
            Prioridad prioridad,
            Long responsableId) {
        List<Solicitud> solicitudes = solicitudRepository.buscarConFiltros(
                estado, tipo, prioridad, responsableId);
        return mapper.toSolicitudDTOList(solicitudes);
    }

    /**
     * Consulta solicitudes por estado.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> consultarPorEstado(EstadoSolicitud estado) {
        return mapper.toSolicitudDTOList(solicitudRepository.findByEstado(estado));
    }

    /**
     * Consulta solicitudes por solicitante.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> consultarPorSolicitante(Long solicitanteId) {
        return mapper.toSolicitudDTOList(solicitudRepository.findBySolicitanteId(solicitanteId));
    }

    /**
     * Consulta solicitudes asignadas a un responsable.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> consultarPorResponsable(Long responsableId) {
        return mapper.toSolicitudDTOList(solicitudRepository.findByResponsableId(responsableId));
    }

    /**
     * Retorna únicamente las solicitudes del solicitante autenticado (ESTUDIANTE / DOCENTE).
     * Un ADMINISTRATIVO ve todas.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> obtenerMisSolicitudes() {
        Usuario actor = actorContextService.obtenerActorActual();
        if (actor.getRol() == Rol.ADMINISTRATIVO) {
            return mapper.toSolicitudDTOList(solicitudRepository.findAll());
        }
        return mapper.toSolicitudDTOList(solicitudRepository.findBySolicitanteId(actor.getId()));
    }

    /**
     * Panel del responsable: solicitudes que tiene asignadas MÁS las que aún
     * no tienen responsable (sin asignar).
     * Un ADMINISTRATIVO ve todas.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> obtenerPanelResponsable() {
        Usuario actor = actorContextService.obtenerActorActual();
        if (actor.getRol() == Rol.ADMINISTRATIVO) {
            return mapper.toSolicitudDTOList(solicitudRepository.findAll());
        }
        return mapper.toSolicitudDTOList(
                solicitudRepository.findByResponsableIdOrUnassigned(actor.getId()));
    }

    /**
     * RF-06: Consulta el historial auditable de una solicitud específica.
     * Retorna las acciones en orden cronológico sin cargar la solicitud completa.
     */
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> obtenerHistorialPorUsuario(Long usuarioId) {
        return historialRepository.findByUsuarioId(usuarioId).stream()
                .map(mapper::toHistorialDTO).collect(java.util.stream.Collectors.toList());
    }

    public List<HistorialResponseDTO> obtenerTodosHistoriales() {
        return historialRepository.findAll().stream()
                .map(mapper::toHistorialDTO).collect(java.util.stream.Collectors.toList());
    }

    public List<HistorialResponseDTO> obtenerHistorial(Long solicitudId) {
        // Verificar que la solicitud existe
        buscarSolicitudPorId(solicitudId);
        return historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId)
                .stream()
                .map(mapper::toHistorialDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * RF-07: Consulta paginada con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<SolicitudResponseDTO> consultarConFiltrosPaginado(
            EstadoSolicitud estado,
            String tipo,
            Prioridad prioridad,
            Long responsableId,
            int page,
            int size,
            String sortBy,
            String direction) {
        String campoOrden = (sortBy == null || sortBy.isBlank()) ? "fechaRegistro" : sortBy;
        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(campoOrden).ascending()
                : Sort.by(campoOrden).descending();

        int pagina = Math.max(page, 0);
        int tamano = size <= 0 ? 10 : Math.min(size, 100);

        Pageable pageable = PageRequest.of(pagina, tamano, sort);
        Page<Solicitud> resultado = solicitudRepository.buscarConFiltrosPaginado(
                estado, tipo, prioridad, responsableId, pageable);

        return PageResponseDTO.<SolicitudResponseDTO>builder()
                .contenido(mapper.toSolicitudDTOList(resultado.getContent()))
                .numeroPagina(resultado.getNumber())
                .tamanoPagina(resultado.getSize())
                .totalElementos(resultado.getTotalElements())
                .totalPaginas(resultado.getTotalPages())
                .ultimaPagina(resultado.isLast())
                .build();
    }

    // ==================== MÉTODOS UTILITARIOS PRIVADOS ====================

    /**
     * Busca una solicitud por ID, lanza excepción si no existe.
     */
    private Solicitud buscarSolicitudPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", id));
    }

    /**
     * RF-04: Valida que la transición de estado sea coherente.
     */
    private void validarTransicion(Solicitud solicitud, EstadoSolicitud nuevoEstado) {
        if (!solicitud.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new TransicionInvalidaException(
                    solicitud.getEstado().getDescripcion(),
                    nuevoEstado.getDescripcion());
        }

        // RF-04/05: Validaciones de negocio específicas para transiciones
        if (nuevoEstado == EstadoSolicitud.EN_ATENCION) {
            if (solicitud.getResponsable() == null) {
                throw new OperacionNoPermitidaException(
                        "No se puede iniciar la atención (EN_ATENCION) sin un RESPONSABLE asignado.");
            }
            if (solicitud.getPrioridad() == null) {
                throw new OperacionNoPermitidaException(
                        "No se puede iniciar la atención (EN_ATENCION) sin haber definido una PRIORIDAD.");
            }
        }
    }

    /**
     * RF-08: Valida que la solicitud no esté cerrada (no permite modificaciones).
     */
    private void validarNoEstaCerrada(Solicitud solicitud) {
        if (solicitud.getEstado() == EstadoSolicitud.CERRADA) {
            throw new OperacionNoPermitidaException(
                    "La solicitud #" + solicitud.getId() + " está cerrada y no puede ser modificada");
        }
    }

    /**
     * RF-06: Crea una entrada de historial con los datos requeridos.
     */
    private HistorialSolicitud crearEntradaHistorial(Solicitud solicitud, Usuario usuario,
            String accion, String observaciones) {
        return HistorialSolicitud.builder()
                .solicitud(solicitud)
                .fechaHora(LocalDateTime.now())
                .accion(accion)
                .usuario(usuario)
                .observaciones(observaciones)
                .build();
    }
}
