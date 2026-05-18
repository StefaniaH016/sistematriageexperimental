import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, UsuarioResponse, UsuarioRequest, Rol } from '../models';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly url = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  crear(dto: UsuarioRequest): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.post<ApiResponse<UsuarioResponse>>(this.url, dto);
  }

  obtenerPorId(id: number): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.get<ApiResponse<UsuarioResponse>>(`${this.url}/${id}`);
  }

  listarTodos(): Observable<ApiResponse<UsuarioResponse[]>> {
    return this.http.get<ApiResponse<UsuarioResponse[]>>(this.url);
  }

  listarPorRol(rol: Rol): Observable<ApiResponse<UsuarioResponse[]>> {
    return this.http.get<ApiResponse<UsuarioResponse[]>>(`${this.url}/rol/${rol}`);
  }

  listarActivos(): Observable<ApiResponse<UsuarioResponse[]>> {
    return this.http.get<ApiResponse<UsuarioResponse[]>>(`${this.url}/activos`);
  }

  listarResponsablesActivos(): Observable<ApiResponse<UsuarioResponse[]>> {
    return this.http.get<ApiResponse<UsuarioResponse[]>>(`${this.url}/responsables-activos`);
  }

  actualizar(id: number, dto: UsuarioRequest): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.put<ApiResponse<UsuarioResponse>>(`${this.url}/${id}`, dto);
  }

  desactivar(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.url}/${id}`);
  }

  activar(id: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.url}/${id}/activar`, {});
  }
}
