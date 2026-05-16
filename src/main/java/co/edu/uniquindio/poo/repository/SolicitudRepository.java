package co.edu.uniquindio.poo.repository;

import co.edu.uniquindio.poo.model.entity.Solicitud;
import co.edu.uniquindio.poo.model.enums.EstadoSolicitud;
import co.edu.uniquindio.poo.model.enums.Prioridad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Solicitud.
 * RF-07: Consulta de solicitudes según diferentes criterios.
 */
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

        /** RF-07: Consultar por estado */
        List<Solicitud> findByEstado(EstadoSolicitud estado);

        /** RF-07: Consultar por tipo de solicitud */
        List<Solicitud> findByTipoSolicitud(String tipoSolicitud);

        /** RF-07: Consultar por prioridad */
        List<Solicitud> findByPrioridad(Prioridad prioridad);

        /** RF-07: Consultar por responsable asignado */
        List<Solicitud> findByResponsableId(Long responsableId);

        /** Consultar por solicitante */
        List<Solicitud> findBySolicitanteId(Long solicitanteId);

        /** Consulta combinada con filtros opcionales */
        @Query("SELECT s FROM Solicitud s WHERE " +
                        "(:estado IS NULL OR s.estado = :estado) AND " +
                        "(:tipo IS NULL OR s.tipoSolicitud = :tipo) AND " +
                        "(:prioridad IS NULL OR s.prioridad = :prioridad) AND " +
                        "(:responsableId IS NULL OR s.responsable.id = :responsableId)")
        List<Solicitud> buscarConFiltros(
                        @Param("estado") EstadoSolicitud estado,
                        @Param("tipo") String tipo,
                        @Param("prioridad") Prioridad prioridad,
                        @Param("responsableId") Long responsableId);

        /** Consulta combinada con filtros opcionales y paginación */
        @Query("SELECT s FROM Solicitud s WHERE " +
                        "(:estado IS NULL OR s.estado = :estado) AND " +
                        "(:tipo IS NULL OR s.tipoSolicitud = :tipo) AND " +
                        "(:prioridad IS NULL OR s.prioridad = :prioridad) AND " +
                        "(:responsableId IS NULL OR s.responsable.id = :responsableId)")
        Page<Solicitud> buscarConFiltrosPaginado(
                        @Param("estado") EstadoSolicitud estado,
                        @Param("tipo") String tipo,
                        @Param("prioridad") Prioridad prioridad,
                        @Param("responsableId") Long responsableId,
                        Pageable pageable);

        /** Contar solicitudes por estado */
        long countByEstado(EstadoSolicitud estado);
}
