import {
  TipoSolicitud,
  EstadoSolicitud,
  Prioridad,
  CanalOrigen,
  Rol
} from './enums';

/* ─── Response DTOs ─── */

export interface UsuarioResponse {
  id: number;
  identificacion: string;
  nombre: string;
  apellido: string;
  email: string;
  rol: Rol;
  activo: boolean;
}

export interface HistorialResponse {
  id: number;
  usuario: UsuarioResponse;
  fechaHora: string;       // ISO datetime
  accion: string;
  observaciones: string;
}

export interface SolicitudResponse {
  id: number;
  titulo: string;
  descripcion: string;
  tipoSolicitud: string;
  estado: EstadoSolicitud;
  prioridad: Prioridad;
  canalOrigen: CanalOrigen;
  fechaCreacion: string;
  fechaLimite: string;
  fechaUltimaActualizacion: string;
  solicitante: UsuarioResponse;
  responsable: UsuarioResponse | null;
  observaciones: string;
  historial: HistorialResponse[];
}

export interface ApiResponse<T> {
  exitoso: boolean;
  mensaje: string;
  datos: T;
  timestamp: string;
}

/* ─── Request DTOs ─── */

export interface SolicitudRequest {
  titulo: string;
  descripcion: string;
  canalOrigen: CanalOrigen;
  solicitanteId: number;
  fechaLimite?: string;    // ISO date
}

export interface ClasificacionRequest {
  tipoSolicitud: string;
  observaciones?: string;
}

export interface PriorizacionRequest {
  prioridad: Prioridad;
  justificacion: string;
}

export interface AsignacionRequest {
  responsableId: number;
  observaciones?: string;
}

export interface CambioEstadoRequest {
  nuevoEstado: EstadoSolicitud;
  observaciones?: string;
}

export interface CierreRequest {
  observacionCierre: string;
}

export interface UsuarioRequest {
  identificacion: string;
  nombre: string;
  apellido: string;
  email: string;
  rol: Rol;
  password: string;
}
