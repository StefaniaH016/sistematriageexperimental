import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models';

export interface SugerenciaIAResponseDTO {
  tipoSolicitudSugerido: string;
  prioridadSugerida: string;
  sugerenciaRespuesta: string;
  resumenHistorial: string;
  justificacionIA: string;
  modeloUtilizado?: string;
}

@Injectable({ providedIn: 'root' })
export class IaService {
  private readonly url = `${environment.apiUrl}/ia/solicitudes`;

  constructor(private http: HttpClient) {}

  obtenerClasificacionSugerida(id: number): Observable<ApiResponse<SugerenciaIAResponseDTO>> {
    return this.http.get<ApiResponse<SugerenciaIAResponseDTO>>(`${this.url}/${id}/clasificacion-sugerida`);
  }
}
