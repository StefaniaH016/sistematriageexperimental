import { Component, OnInit, Pipe, PipeTransform, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { SolicitudService } from '../../services/solicitud.service';
import { UsuarioService } from '../../services/usuario.service';
import { AuthService } from '../../services/auth.service';
import { IaService, SugerenciaIAResponseDTO } from '../../services/ia.service';
import {
  SolicitudResponse,
  UsuarioResponse,
  HistorialResponse,
  EstadoSolicitud,
  TipoSolicitud,
  Prioridad,
  Rol,
  ESTADO_LABELS,
  PRIORIDAD_LABELS,
  TIPO_SOLICITUD_LABELS,
  CANAL_LABELS,
  CanalOrigen
} from '../../models';

@Pipe({ name: 'anyToTipo', standalone: true })
export class AnyToTipoPipe implements PipeTransform {
  transform(value: any): TipoSolicitud { return value as TipoSolicitud; }
}

@Pipe({ name: 'anyToPrioridad', standalone: true })
export class AnyToPrioridadPipe implements PipeTransform {
  transform(value: any): Prioridad { return value as Prioridad; }
}

@Component({
  selector: 'app-solicitud-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnyToTipoPipe, AnyToPrioridadPipe],
  template: `
    <div class="page">
      @if (cargando) {
        <div class="loading-container">
          <div class="spinner"></div>
          <span>Cargando solicitud...</span>
        </div>
      } @else if (!solicitud) {
        <div class="empty-state">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/>
            <path d="M16 16s-1.5-2-4-2-4 2-4 2"/>
            <line x1="9" y1="9" x2="9.01" y2="9"/>
            <line x1="15" y1="9" x2="15.01" y2="9"/>
          </svg>
          <h3>Solicitud no encontrada</h3>
          <a routerLink="/solicitudes" class="btn btn-primary">Volver a solicitudes</a>
        </div>
      } @else {
        <!-- Header -->
        <div class="page-header">
          <a routerLink="/solicitudes" class="btn-back">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
            Volver
          </a>
        </div>

        <div class="detail-header">
          <div class="header-info">
            <span class="solicitud-id">#{{ solicitud.id }}</span>
            <h1 class="solicitud-title">{{ solicitud.titulo }}</h1>
          </div>
          <div class="header-badges">
            <span class="badge badge-lg" [class]="'badge-estado-' + solicitud.estado.toLowerCase()">
              {{ estadoLabel(solicitud.estado) }}
            </span>
            @if (solicitud.prioridad) {
              <span class="badge badge-lg" [class]="'badge-prioridad-' + solicitud.prioridad.toLowerCase()">
                {{ prioridadLabel(solicitud.prioridad) }}
              </span>
            }
          </div>
        </div>

        @if (mensaje) {
          <div class="alert" [class.alert-success]="!esError" [class.alert-error]="esError">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              @if (esError) {
                <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
              } @else {
                <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22,4 12,14.01 9,11.01"/>
              }
            </svg>
            {{ mensaje }}
          </div>
        }

        <div class="detail-grid">
          <!-- Info Card -->
          <div class="card info-card">
            <div class="card-header-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <h2>Información General</h2>
            </div>
            <div class="info-grid">
              <div class="info-item full-width">
                <span class="info-label">Descripción</span>
                <p class="info-value desc">{{ solicitud.descripcion }}</p>
              </div>
              <div class="info-item">
                <span class="info-label">Tipo</span>
                <span class="info-value">
                  @if (solicitud.tipoSolicitud) {
                    <span class="tipo-badge">{{ solicitud.tipoSolicitud }}</span>
                  } @else {
                    <em class="text-muted">Sin clasificar</em>
                  }
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">Canal de Origen</span>
                <span class="info-value">{{ canalLabel(solicitud.canalOrigen) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Solicitante</span>
                <span class="info-value user-value">
                  <span class="avatar-sm">{{ solicitud.solicitante.nombre.charAt(0) }}</span>
                  {{ solicitud.solicitante.nombre }} {{ solicitud.solicitante.apellido }}
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">Responsable</span>
                <span class="info-value user-value">
                  @if (solicitud.responsable) {
                    <span class="avatar-sm">{{ solicitud.responsable.nombre.charAt(0) }}</span>
                    {{ solicitud.responsable.nombre }} {{ solicitud.responsable.apellido }}
                  } @else {
                    <em class="text-muted">Sin asignar</em>
                  }
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">Fecha Creación</span>
                <span class="info-value">{{ solicitud.fechaCreacion | date:'dd/MM/yyyy HH:mm' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Fecha Límite</span>
                <span class="info-value">
                  @if (solicitud.fechaLimite) {
                    {{ solicitud.fechaLimite | date:'dd/MM/yyyy' }}
                  } @else {
                    <em class="text-muted">No definida</em>
                  }
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">Última Actualización</span>
                <span class="info-value">{{ solicitud.fechaUltimaActualizacion | date:'dd/MM/yyyy HH:mm' }}</span>
              </div>
              @if (solicitud.observaciones) {
                <div class="info-item full-width">
                  <span class="info-label">Observaciones</span>
                  <p class="info-value desc obs">{{ solicitud.observaciones }}</p>
                </div>
              }
            </div>
          </div>

          <!-- Actions Panel -->
          @if (solicitud.estado !== 'CERRADA' && (authService.currentUserValue?.rol === 'RESPONSABLE' || authService.currentUserValue?.rol === 'ADMINISTRATIVO')) {
            <div class="card actions-card">
              <div class="card-header-title">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/>
                </svg>
                <h2>Acciones</h2>
              </div>

              <!-- RF-02: Clasificar (Solo Administrativo) -->
              @if (authService.currentUserValue?.rol === 'ADMINISTRATIVO' && solicitud.estado === 'REGISTRADA') {
                <div class="action-section">
                  <div class="action-header">
                    <h3>Clasificar Solicitud</h3>
                    <button class="btn-ia" (click)="sugerirIA()" [disabled]="cargandoIA">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
                      </svg>
                      {{ cargandoIA ? 'Analizando...' : 'IA Sugerencia' }}
                    </button>
                  </div>
                  
                  @if (sugerenciaIA) {
                    <div class="ia-suggestion">
                      <div class="ia-badge" [class.rules-badge]="sugerenciaIA.modeloUtilizado === 'REGLAS_NEGOCIO'">
                        {{ sugerenciaIA.modeloUtilizado === 'REGLAS_NEGOCIO' ? 'SISTEMA' : 'IA' }}
                      </div>
                      <div class="ia-content">
                        <div class="ia-main-suggestion">
                          <strong>Sugerencia:</strong> {{ tipoLabel(sugerenciaIA.tipoSolicitudSugerido | anyToTipo) }}
                          @if (sugerenciaIA.prioridadSugerida) {
                            <span class="sep">|</span>
                            <strong>Prioridad:</strong> {{ prioridadLabel(sugerenciaIA.prioridadSugerida | anyToPrioridad) }}
                          }
                        </div>
                        @if (sugerenciaIA.justificacionIA) {
                          <p class="ia-reason">{{ sugerenciaIA.justificacionIA }}</p>
                        }
                        @if (sugerenciaIA.resumenHistorial) {
                          <div class="ia-resumen-box">
                            <strong>Resumen del Historial:</strong>
                            <p class="ia-resumen">{{ sugerenciaIA.resumenHistorial }}</p>
                          </div>
                        }
                        @if (sugerenciaIA.sugerenciaRespuesta) {
                          <div class="ia-resumen-box">
                            <strong>Borrador de respuesta:</strong>
                            <p class="ia-resumen">{{ sugerenciaIA.sugerenciaRespuesta }}</p>
                          </div>
                        }
                      </div>
                      <button class="btn-icon" (click)="aplicarSugerenciaIA()" title="Aplicar sugerencia">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polyline points="20,6 9,17 4,12"/>
                        </svg>
                      </button>
                    </div>
                  }

                  <div class="action-form">
                    <div class="input-group">
                      <input type="text" [(ngModel)]="clasificarTipo" placeholder="Tipo de solicitud (ej: Supletorio, Homologación)..." class="form-input" list="tipos-comunes">
                      <datalist id="tipos-comunes">
                        @for (t of tipos; track t) {
                          <option [value]="tipoLabel(t)">
                        }
                      </datalist>
                    </div>
                    <input type="text" [(ngModel)]="clasificarObs" placeholder="Observaciones..." class="form-input">
                    <button class="btn btn-action" [disabled]="!clasificarTipo" (click)="clasificar()">Clasificar</button>
                  </div>
                </div>
              }

              <!-- RF-03: Priorizar (Solo Administrativo) -->
              @if (authService.currentUserValue?.rol === 'ADMINISTRATIVO' && (solicitud.estado === 'CLASIFICADA' || solicitud.estado === 'REGISTRADA')) {
                <div class="action-section">
                  <h3>Priorizar Solicitud</h3>
                  <div class="action-form">
                    <select [(ngModel)]="priorizarPrioridad" class="form-input">
                      <option value="">Seleccione prioridad...</option>
                      @for (p of prioridadesEnum; track p) {
                        <option [value]="p">{{ prioridadLabel(p) }}</option>
                      }
                    </select>
                    <input type="text" [(ngModel)]="priorizarObs" placeholder="Justificación obligatoria *" class="form-input" required>
                    <button class="btn btn-action" [disabled]="!priorizarPrioridad || !priorizarObs" (click)="priorizar()">Priorizar</button>
                  </div>
                </div>
              }

              <!-- RF-05: Asignar (Solo Administrativo) -->
              @if (authService.currentUserValue?.rol === 'ADMINISTRATIVO' && !solicitud.responsable) {
                <div class="action-section">
                  <h3>Asignar Responsable</h3>
                  <div class="action-form">
                    <select [(ngModel)]="asignarResponsableId" class="form-input">
                      <option [ngValue]="0" disabled>Seleccione responsable...</option>
                      @for (r of responsables; track r.id) {
                        <option [ngValue]="r.id">{{ r.nombre }} {{ r.apellido }}</option>
                      }
                    </select>
                    <input type="text" [(ngModel)]="asignarObs" placeholder="Observaciones obligatorias *" class="form-input" required>
                    <button class="btn btn-action" [disabled]="asignarResponsableId === 0 || !asignarObs" (click)="asignar()">Asignar</button>
                  </div>
                </div>
              }

              <!-- RF-04: Cambiar estado (Admin o Responsable Asignado) -->
              @if (authService.currentUserValue?.rol === 'ADMINISTRATIVO' || (authService.currentUserValue?.rol === 'RESPONSABLE' && solicitud.responsable?.id === authService.currentUserValue?.id)) {
                <div class="action-section">
                  <h3>Cambiar Estado</h3>
                  <div class="action-form">
                    <select [(ngModel)]="nuevoEstado" class="form-input">
                      @for (e of estadosDisponibles; track e) {
                        <option [value]="e">{{ estadoLabel(e) }}</option>
                      }
                    </select>
                    <input type="text" [(ngModel)]="estadoObs" placeholder="Observaciones obligatorias *" class="form-input" required>
                    <button class="btn btn-action" [disabled]="!estadoObs || (nuevoEstado === 'EN_ATENCION' && (!solicitud.responsable || !solicitud.prioridad))" (click)="cambiarEstado()">Cambiar</button>
                    @if (nuevoEstado === 'EN_ATENCION' && (!solicitud.responsable || !solicitud.prioridad)) {
                      <p style="color: #b85450; font-size: 0.75rem; margin: 0.25rem 0 0 0;">
                        * Requiere responsable y prioridad para iniciar atención.
                      </p>
                    }
                  </div>
                </div>
              }

              <!-- RF-08: Cerrar (Solo Administrativo) -->
              @if (authService.currentUserValue?.rol === 'ADMINISTRATIVO' && solicitud.estado === 'ATENDIDA') {
                <div class="action-section action-cerrar">
                  <h3>Cerrar Solicitud</h3>
                  <div class="action-form">
                    <input type="text" [(ngModel)]="cerrarObs" placeholder="Observaciones de cierre *" class="form-input" required>
                    <button class="btn btn-danger" [disabled]="!cerrarObs" (click)="cerrar()">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                        <path d="M7 11V7a5 5 0 0110 0v4"/>
                      </svg>
                      Cerrar Solicitud
                    </button>
                  </div>
                </div>
              }
            </div>
          }

          <!-- RF-06: Historial / Trazabilidad -->
          <div class="card historial-card">
            <div class="card-header-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/>
              </svg>
              <h2>Historial de Trazabilidad</h2>
            </div>
            @if (historial.length === 0) {
              <p class="text-muted">No hay registros de historial.</p>
            } @else {
              <div class="timeline">
                @for (h of historial; track h.id) {
                  <div class="timeline-item">
                    <div class="timeline-dot"></div>
                    <div class="timeline-content">
                      <div class="timeline-header">
                        <strong>{{ h.accion }}</strong>
                        <span class="timeline-date">{{ h.fechaHora | date:'dd/MM/yyyy HH:mm' }}</span>
                      </div>
                      @if (h.usuario) {
                        <div class="timeline-user">
                          <span class="avatar-xs">{{ h.usuario.nombre.charAt(0) }}</span>
                          {{ h.usuario.nombre }} {{ h.usuario.apellido }}
                        </div>
                      }
                      @if (h.observaciones) {
                        <div class="timeline-obs">{{ h.observaciones }}</div>
                      }
                    </div>
                  </div>
                }
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .page {
      padding: 2rem 3rem;
      min-height: calc(100vh - 60px);
      background: var(--color-bg, #f7f5f2);
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
      color: var(--color-text-secondary, #6b6b6b);
      gap: 1rem;
    }

    .spinner {
      width: 32px;
      height: 32px;
      border: 3px solid var(--color-border, #e0dcd5);
      border-top-color: var(--color-primary, #8b7355);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 2rem;
      text-align: center;
      color: var(--color-text-secondary, #6b6b6b);
      background: white;
      border-radius: 12px;
      max-width: 400px;
      margin: 2rem auto;
    }

    .empty-state svg { opacity: 0.4; margin-bottom: 1rem; }
    .empty-state h3 { margin: 0 0 1rem 0; color: var(--color-text, #3d3d3d); }

    .page-header { margin-bottom: 1rem; }

    .btn-back {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      background: transparent;
      border: none;
      color: var(--color-text-secondary, #6b6b6b);
      font-size: 0.9rem;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.2s ease;
      text-decoration: none;
    }

    .btn-back:hover {
      color: var(--color-text, #3d3d3d);
      background: var(--color-bg-alt, #edeae5);
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;
      background: white;
      padding: 1.5rem;
      border-radius: 12px;
      box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.04));
    }

    .solicitud-id {
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--color-primary, #8b7355);
      display: block;
      margin-bottom: 0.25rem;
    }

    .solicitud-title {
      font-size: 1.4rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      margin: 0;
      line-height: 1.3;
    }

    .header-badges {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .badge {
      padding: 0.3rem 0.75rem;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      border-radius: 6px;
    }

    .badge-lg { padding: 0.4rem 1rem; font-size: 0.8rem; }

    .badge-estado-registrada { background: #e3f2fd; color: #1565c0; }
    .badge-estado-clasificada { background: #fff3e0; color: #e65100; }
    .badge-estado-en_atencion { background: #e8f5e9; color: #2e7d32; }
    .badge-estado-atendida { background: #f3e5f5; color: #7b1fa2; }
    .badge-estado-cerrada { background: var(--color-bg-alt, #edeae5); color: var(--color-text-secondary, #6b6b6b); }

    .badge-prioridad-baja { background: #e8f5e9; color: #388e3c; }
    .badge-prioridad-media { background: #fff8e1; color: #f9a825; }
    .badge-prioridad-alta { background: #ffebee; color: #c62828; }
    .badge-prioridad-critica { background: #c62828; color: white; }

    .alert {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem 1.25rem;
      border-radius: 10px;
      margin-bottom: 1.5rem;
      font-size: 0.9rem;
    }

    .alert-error { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }
    .alert-success { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }

    .detail-grid {
      display: grid;
      grid-template-columns: 1.2fr 1fr;
      gap: 1.5rem;
      align-items: start;
    }

    .card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.04));
    }

    .card-header-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1.25rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--color-border, #e0dcd5);
    }

    .card-header-title svg { color: var(--color-primary, #8b7355); }

    .card-header-title h2 {
      font-size: 1rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      margin: 0;
    }

    .info-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.25rem;
    }

    .info-item { display: flex; flex-direction: column; gap: 0.3rem; }
    .info-item.full-width { grid-column: 1 / -1; }

    .info-label {
      font-size: 0.7rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--color-text-secondary, #6b6b6b);
    }

    .info-value {
      font-size: 0.95rem;
      color: var(--color-text, #3d3d3d);
      line-height: 1.5;
    }

    .info-value.desc {
      background: var(--color-bg, #f7f5f2);
      padding: 0.75rem 1rem;
      border-radius: 8px;
      margin: 0;
    }

    .info-value.obs {
      border-left: 3px solid var(--color-primary, #8b7355);
    }

    .tipo-badge {
      display: inline-block;
      background: var(--color-bg-alt, #edeae5);
      padding: 0.25rem 0.6rem;
      border-radius: 4px;
      font-size: 0.85rem;
    }

    .user-value {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .avatar-sm {
      width: 24px;
      height: 24px;
      background: var(--color-primary, #8b7355);
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.7rem;
      font-weight: 600;
    }

    .avatar-xs {
      width: 20px;
      height: 20px;
      background: var(--color-primary-light, #d4c8b8);
      color: var(--color-primary-hover, #6d5a44);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.6rem;
      font-weight: 600;
    }

    .text-muted { color: var(--color-text-muted, #9a9a9a); font-style: italic; }

    /* Actions */
    .action-section {
      padding: 1rem;
      margin-bottom: 1rem;
      background: var(--color-bg, #f7f5f2);
      border-radius: 10px;
    }

    .action-section:last-child { margin-bottom: 0; }

    .action-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
    }

    .action-section h3 {
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      margin: 0 0 0.75rem 0;
    }

    .action-header h3 { margin: 0; }

    .action-form {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .form-input {
      padding: 0.6rem 0.75rem;
      border: 1px solid var(--color-border, #e0dcd5);
      border-radius: 8px;
      font-size: 0.9rem;
      background: white;
      color: var(--color-text, #3d3d3d);
      transition: border-color 0.2s ease;
    }

    .form-input:focus {
      outline: none;
      border-color: var(--color-primary, #8b7355);
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.6rem 1rem;
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
      border: none;
    }

    .btn-primary {
      background: var(--color-primary, #8b7355);
      color: white;
    }

    .btn-primary:hover { background: var(--color-primary-hover, #6d5a44); }

    .btn-action {
      background: var(--color-primary, #8b7355);
      color: white;
      align-self: flex-start;
    }

    .btn-action:hover:not(:disabled) { background: var(--color-primary-hover, #6d5a44); }
    .btn-action:disabled { opacity: 0.5; cursor: not-allowed; }

    .btn-danger {
      background: var(--color-danger, #b85450);
      color: white;
    }

    .btn-danger:hover:not(:disabled) { background: #9c3c38; }
    .btn-danger:disabled { opacity: 0.5; cursor: not-allowed; }

    .btn-ia {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.4rem 0.75rem;
      background: white;
      border: 1px solid var(--color-border, #e0dcd5);
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 500;
      color: var(--color-text-secondary, #6b6b6b);
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-ia:hover:not(:disabled) {
      border-color: var(--color-primary, #8b7355);
      color: var(--color-primary, #8b7355);
    }

    .btn-ia:disabled { opacity: 0.5; cursor: not-allowed; }

    .ia-suggestion {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      background: white;
      border: 1px solid var(--color-primary-light, #d4c8b8);
      border-radius: 8px;
      padding: 0.75rem 1rem;
      margin-bottom: 0.75rem;
    }

    .ia-badge {
      background: var(--color-primary, #8b7355);
      color: white;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.65rem;
      font-weight: 700;
      letter-spacing: 1px;
    }
    .rules-badge { background: var(--color-text-secondary, #6b6b6b); }

    .ia-content { flex: 1; font-size: 0.85rem; }
    .ia-main-suggestion {
      margin-bottom: 0.25rem;
      font-weight: 500;
    }

    .ia-main-suggestion .sep {
      margin: 0 0.5rem;
      color: var(--color-border, #e0dcd5);
    }

    .ia-reason {
      margin: 0.4rem 0;
      font-style: italic;
      color: var(--color-text-secondary, #6b6b6b);
      font-size: 0.8rem;
    }

    .ia-resumen-box {
      margin-top: 0.75rem;
      padding-top: 0.75rem;
      border-top: 1px solid var(--color-bg, #f7f5f2);
    }

    .ia-resumen-box strong {
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--color-text-secondary, #6b6b6b);
      display: block;
      margin-bottom: 0.25rem;
    }

    .ia-resumen {
      margin: 0;
      font-size: 0.85rem;
      color: var(--color-text, #3d3d3d);
      line-height: 1.4;
      background: var(--color-bg, #f7f5f2);
      padding: 0.5rem 0.75rem;
      border-radius: 6px;
    }

    .btn-icon {
      background: transparent;
      border: 1px solid var(--color-success, #5d8a66);
      color: var(--color-success, #5d8a66);
      border-radius: 6px;
      padding: 0.4rem;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-icon:hover {
      background: var(--color-success, #5d8a66);
      color: white;
    }

    .action-cerrar {
      background: #fef2f2;
      border: 1px dashed var(--color-danger, #b85450);
    }

    /* Historial */
    .historial-card {
      grid-column: 1 / -1;
    }

    .timeline {
      position: relative;
      padding-left: 1.5rem;
    }

    .timeline::before {
      content: '';
      position: absolute;
      left: 6px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: var(--color-border, #e0dcd5);
    }

    .timeline-item {
      position: relative;
      margin-bottom: 1.25rem;
    }

    .timeline-item:last-child { margin-bottom: 0; }

    .timeline-dot {
      position: absolute;
      left: -1.5rem;
      top: 4px;
      width: 12px;
      height: 12px;
      background: white;
      border: 2px solid var(--color-primary, #8b7355);
      border-radius: 50%;
    }

    .timeline-content {
      background: var(--color-bg, #f7f5f2);
      padding: 0.75rem 1rem;
      border-radius: 8px;
    }

    .timeline-header {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      margin-bottom: 0.25rem;
    }

    .timeline-header strong {
      font-size: 0.85rem;
      color: var(--color-text, #3d3d3d);
    }

    .timeline-date {
      font-size: 0.75rem;
      color: var(--color-text-muted, #9a9a9a);
    }

    .timeline-user {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 0.8rem;
      color: var(--color-text-secondary, #6b6b6b);
      margin-top: 0.25rem;
    }

    .timeline-obs {
      margin-top: 0.5rem;
      font-size: 0.85rem;
      color: var(--color-text, #3d3d3d);
      padding-left: 0.75rem;
      border-left: 2px solid var(--color-primary-light, #d4c8b8);
    }

    @media (max-width: 900px) {
      .page { padding: 1.5rem 1rem; }
      .detail-grid { grid-template-columns: 1fr; }
      .detail-header { flex-direction: column; gap: 1rem; }
      .info-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class SolicitudDetailComponent implements OnInit {
  solicitud: SolicitudResponse | null = null;
  historial: HistorialResponse[] = [];
  responsables: UsuarioResponse[] = [];
  cargando = true;
  mensaje = '';
  esError = false;

  cargandoIA = false;
  sugerenciaIA: SugerenciaIAResponseDTO | null = null;

  // Form models
  clasificarTipo: string = '';
  clasificarObs = '';
  priorizarPrioridad: string = '';
  priorizarObs = '';
  asignarResponsableId = 0;
  asignarObs = '';
  nuevoEstado: EstadoSolicitud = EstadoSolicitud.CLASIFICADA;
  estadosDisponibles: EstadoSolicitud[] = [];
  estadoObs = '';
  cerrarObs = '';

  tipos = Object.values(TipoSolicitud);
  prioridadesEnum = Object.values(Prioridad);

  private solicitudId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private solicitudService: SolicitudService,
    private usuarioService: UsuarioService,
    private iaService: IaService,
    private cdr: ChangeDetectorRef,
    public authService: AuthService
  ) { }

  ngOnInit(): void {
    this.solicitudId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
    this.usuarioService.listarPorRol(Rol.RESPONSABLE).pipe(
      finalize(() => this.cdr.markForCheck())
    ).subscribe({
      next: res => {
        if (res.exitoso) this.responsables = res.datos;
        this.cdr.markForCheck();
      },
      error: (err) => { console.error('Error al cargar responsables:', err); }
    });
  }

  cargar(): void {
    this.cargando = true;
    this.solicitudService.obtenerPorId(this.solicitudId).pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: res => {
        if (res.exitoso) {
          this.solicitud = res.datos;
          this.historial = res.datos.historial || [];
          this.actualizarEstadosDisponibles();
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error al cargar solicitud:', err);
      }
    });
  }

  actualizarEstadosDisponibles(): void {
    if (!this.solicitud) {
      this.estadosDisponibles = [];
      return;
    }
    const transiciones: Record<string, EstadoSolicitud[]> = {
      'REGISTRADA': [EstadoSolicitud.CLASIFICADA],
      'CLASIFICADA': [EstadoSolicitud.EN_ATENCION],
      'EN_ATENCION': [EstadoSolicitud.ATENDIDA],
      'ATENDIDA': [EstadoSolicitud.CERRADA],
      'CERRADA': []
    };
    this.estadosDisponibles = transiciones[this.solicitud.estado] || [];
    if (this.estadosDisponibles.length > 0) {
      this.nuevoEstado = this.estadosDisponibles[0];
    }
  }

  sugerirIA(): void {
    this.cargandoIA = true;
    this.sugerenciaIA = null;
    this.iaService.obtenerClasificacionSugerida(this.solicitudId).pipe(
      finalize(() => {
        this.cargandoIA = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (res) => {
        if (res.exitoso) {
          this.sugerenciaIA = res.datos;
        } else {
          this.onError({ error: { mensaje: 'No se pudo obtener sugerencia IA, pero puede continuar manualmente.' } });
        }
        this.cdr.markForCheck();
      },
      error: () => {
        // Gracia (Fallback) RF-11
        this.onError({ error: { mensaje: 'Servicio IA no disponible. La aplicación sigue operando normalmente.' } });
      }
    });
  }

  aplicarSugerenciaIA(): void {
    if (this.sugerenciaIA) {
      this.clasificarTipo = this.sugerenciaIA.tipoSolicitudSugerido;
      this.priorizarPrioridad = this.sugerenciaIA.prioridadSugerida;
      this.clasificarObs = `Sugerencia IA Aplicada. Razón: ${this.sugerenciaIA.justificacionIA}`;
      this.priorizarObs = 'Prioridad sugerida por IA basada en el análisis de la solicitud.';
    }
  }

  clasificar(): void {
    this.solicitudService.clasificar(this.solicitudId, {
      tipoSolicitud: this.clasificarTipo,
      observaciones: this.clasificarObs || undefined
    }).subscribe({
      next: () => this.onSuccess('Solicitud clasificada correctamente'),
      error: err => this.onError(err)
    });
  }

  priorizar(): void {
    if (!this.priorizarPrioridad || !this.priorizarObs) {
      this.onError({ error: { mensaje: 'La prioridad y su justificación son obligatorias' } });
      return;
    }
    this.solicitudService.priorizar(this.solicitudId, {
      prioridad: this.priorizarPrioridad as Prioridad,
      justificacion: this.priorizarObs
    }).subscribe({
      next: () => {
        this.priorizarObs = '';
        this.onSuccess('Solicitud priorizada correctamente');
      },
      error: err => this.onError(err)
    });
  }

  asignar(): void {
    if (this.asignarResponsableId === 0 || !this.asignarObs) {
      this.onError({ error: { mensaje: 'El responsable y la observación son obligatorios' } });
      return;
    }
    this.solicitudService.asignar(this.solicitudId, {
      responsableId: this.asignarResponsableId,
      observaciones: this.asignarObs
    }).subscribe({
      next: () => {
        this.asignarObs = '';
        this.onSuccess('Responsable asignado correctamente');
      },
      error: err => this.onError(err)
    });
  }

  cambiarEstado(): void {
    if (!this.estadoObs) {
      this.onError({ error: { mensaje: 'La observación del cambio de estado es obligatoria' } });
      return;
    }
    this.solicitudService.cambiarEstado(this.solicitudId, {
      nuevoEstado: this.nuevoEstado,
      observaciones: this.estadoObs
    }).subscribe({
      next: () => {
        this.estadoObs = '';
        this.onSuccess('Estado cambiado correctamente');
      },
      error: err => this.onError(err)
    });
  }

  cerrar(): void {
    if (!this.cerrarObs) {
      this.onError({ error: { mensaje: 'La observación de cierre es obligatoria' } });
      return;
    }
    this.solicitudService.cerrar(this.solicitudId, {
      observacionCierre: this.cerrarObs
    }).subscribe({
      next: () => {
        this.cerrarObs = '';
        this.onSuccess('Solicitud cerrada correctamente');
      },
      error: err => this.onError(err)
    });
  }

  private onSuccess(msg: string): void {
    this.mensaje = msg;
    this.esError = false;
    this.cargar();
    setTimeout(() => this.mensaje = '', 4000);
  }

  private onError(err: any): void {
    this.mensaje = err.error?.mensaje || 'Error al procesar la operación';
    this.esError = true;
    setTimeout(() => this.mensaje = '', 5000);
  }

  estadoLabel(e: EstadoSolicitud): string { return ESTADO_LABELS[e] || e; }
  prioridadLabel(p: Prioridad): string { return PRIORIDAD_LABELS[p] || p; }
  tipoLabel(t: any): string { return TIPO_SOLICITUD_LABELS[t as TipoSolicitud] || t; }
  canalLabel(c: CanalOrigen): string { return CANAL_LABELS[c] || c; }
}
