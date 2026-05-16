import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApiResponse,
  SolicitudResponse,
  SolicitudRequest,
  ClasificacionRequest,
  PriorizacionRequest,
  AsignacionRequest,
  CambioEstadoRequest,
  CierreRequest,
  EstadoSolicitud,
  TipoSolicitud,
  Prioridad
} from '../models';

@Injectable({ providedIn: 'root' })
export class SolicitudService {
  private readonly url = `${environment.apiUrl}/solicitudes`;

  constructor(private http: HttpClient) {}

  /** RF-01: Registrar nueva solicitud */
  registrar(dto: SolicitudRequest): Observable<ApiResponse<SolicitudResponse>> {
    return this.http.post<ApiResponse<SolicitudResponse>>(this.url, dto);
  }

  /** RF-02: Clasificar solicitud */
  clasificar(id: number, dto: ClasificacionRequest): Observable<ApiResponse<SolicitudResponse>> {
    return this.http.put<ApiResponse<SolicitudResponse>>(`${this.url}/${id}/clasificar`, dto);
  }

  /** RF-03: Priorizar solicitud */
  priorizar(id: number, dto: PriorizacionRequest): Observable<ApiResponse<SolicitudResponse>> {
    return this.http.put<ApiResponse<SolicitudResponse>>(`${this.url}/${id}/priorizar`, dto);
  }

  /** RF-04: Cambiar estado */
  cambiarEstado(id: number, dto: CambioEstadoRequest): Observable<ApiResponse<SolicitudResponse>> {
    return this.http.put<ApiResponse<SolicitudResponse>>(`${this.url}/${id}/estado`, dto);
  }

  /** RF-05: Asignar responsable */
  asignar(id: number, dto: AsignacionRequest): Observable<ApiResponse<SolicitudResponse>> {
    return this.http.put<ApiResponse<SolicitudResponse>>(`${this.url}/${id}/asignar`, dto);
  }

  /** RF-08: Cerrar solicitud */
  cerrar(id: number, dto: CierreRequest): Observable<ApiResponse<SolicitudResponse>> {
    return this.http.put<ApiResponse<SolicitudResponse>>(`${this.url}/${id}/cerrar`, dto);
  }

  /** RF-06: Obtener historial */
  obtenerHistorial(id: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.url}/${id}/historial`);
  }

  /** Obtener solicitud por ID */
  obtenerPorId(id: number): Observable<ApiResponse<SolicitudResponse>> {
    return this.http.get<ApiResponse<SolicitudResponse>>(`${this.url}/${id}`);
  }

  /** RF-07: Listar con filtros */
  listar(filtros?: {
    estado?: EstadoSolicitud;
    tipo?: string;
    prioridad?: Prioridad;
    responsableId?: number;
  }): Observable<ApiResponse<SolicitudResponse[]>> {
    let params = new HttpParams();
    if (filtros) {
      if (filtros.estado) params = params.set('estado', filtros.estado);
      if (filtros.tipo) params = params.set('tipo', filtros.tipo);
      if (filtros.prioridad) params = params.set('prioridad', filtros.prioridad);
      if (filtros.responsableId) params = params.set('responsableId', filtros.responsableId.toString());
    }
    return this.http.get<ApiResponse<SolicitudResponse[]>>(this.url, { params });
  }

  /** Listar todas */
  listarTodas(): Observable<ApiResponse<SolicitudResponse[]>> {
    return this.http.get<ApiResponse<SolicitudResponse[]>>(this.url);
  }
}
