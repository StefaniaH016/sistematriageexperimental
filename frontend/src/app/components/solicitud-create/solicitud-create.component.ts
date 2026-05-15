import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { SolicitudService } from '../../services/solicitud.service';
import { UsuarioService } from '../../services/usuario.service';
import { AuthService } from '../../services/auth.service';
import { DataRefreshService } from '../../services/data-refresh.service';
import {
  SolicitudRequest,
  UsuarioResponse,
  CanalOrigen,
  CANAL_LABELS,
  Rol,
  ROL_LABELS
} from '../../models';

@Component({
  selector: 'app-solicitud-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <button class="btn-back" (click)="cancelar()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Volver
        </button>
      </div>

      <div class="form-container">
        <div class="form-header">
          <div class="form-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
              <polyline points="14,2 14,8 20,8"/>
              <line x1="12" y1="18" x2="12" y2="12"/>
              <line x1="9" y1="15" x2="15" y2="15"/>
            </svg>
          </div>
          <h1>Nueva Solicitud</h1>
          <p>Registra una nueva solicitud académica</p>
        </div>

        @if (mensajeError) {
          <div class="alert alert-error">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            {{ mensajeError }}
          </div>
        }
        @if (mensajeExito) {
          <div class="alert alert-success">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
              <polyline points="22,4 12,14.01 9,11.01"/>
            </svg>
            {{ mensajeExito }}
          </div>
        }

        <form (ngSubmit)="enviar()" #form="ngForm" class="form-card">
          <div class="form-section">
            <label for="titulo" class="form-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Título
            </label>
            <input id="titulo" type="text" [(ngModel)]="solicitud.titulo" name="titulo"
              required minlength="5" #titulo="ngModel"
              placeholder="Ej: Solicitud de homologación de materias"
              class="form-input">
            @if (titulo.invalid && titulo.touched) {
              <span class="field-error">El título debe tener al menos 5 caracteres</span>
            }
          </div>

          <div class="form-section">
            <label for="descripcion" class="form-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="17" y1="10" x2="3" y2="10"/>
                <line x1="21" y1="6" x2="3" y2="6"/>
                <line x1="21" y1="14" x2="3" y2="14"/>
                <line x1="17" y1="18" x2="3" y2="18"/>
              </svg>
              Descripción
            </label>
            <textarea id="descripcion" [(ngModel)]="solicitud.descripcion" name="descripcion"
              required minlength="10" #desc="ngModel" rows="4"
              placeholder="Describe detalladamente tu solicitud académica..."
              class="form-input form-textarea"></textarea>
            @if (desc.invalid && desc.touched) {
              <span class="field-error">La descripción debe tener al menos 10 caracteres</span>
            }
          </div>

          <div class="form-row">
            <div class="form-section">
              <label for="canal" class="form-label">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
                </svg>
                Canal de Origen
              </label>
              <select id="canal" [(ngModel)]="solicitud.canalOrigen" name="canalOrigen" required class="form-input">
                @for (c of canales; track c) {
                  <option [value]="c">{{ canalLabel(c) }}</option>
                }
              </select>
            </div>

            <div class="form-section">
              <label for="solicitante" class="form-label">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                Solicitante
              </label>
              @if (authService.currentUserValue?.rol === 'ESTUDIANTE') {
                <div class="form-input readonly-field">
                  {{ authService.currentUserValue?.nombre }} {{ authService.currentUserValue?.apellido }}
                  <span class="badge-rol">ESTUDIANTE</span>
                </div>
              } @else {
                @if (cargandoUsuarios) {
                  <div class="loading-select">
                    <span class="spinner-xs"></span>
                    Cargando usuarios...
                  </div>
                } @else if (usuarios.length === 0) {
                  <div class="empty-select">
                    <span>No hay usuarios disponibles</span>
                    <button type="button" class="btn-retry" (click)="cargarUsuarios()">
                      Reintentar
                    </button>
                  </div>
                } @else {
                  <select id="solicitante" [(ngModel)]="solicitud.solicitanteId" name="solicitanteId" required class="form-input">
                    <option [ngValue]="0" disabled>Seleccione un solicitante...</option>
                    @for (u of usuarios; track u.id) {
                      <option [ngValue]="u.id">{{ u.nombre }} {{ u.apellido }} ({{ rolLabel(u.rol) }})</option>
                    }
                  </select>
                  <span class="field-hint">{{ usuarios.length }} usuario(s) disponible(s)</span>
                }
              }
            </div>
          </div>

          <div class="form-section">
            <label for="fechaLimite" class="form-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              Fecha Límite <span class="optional">(opcional)</span>
            </label>
            <input id="fechaLimite" type="date" [(ngModel)]="solicitud.fechaLimite" name="fechaLimite" class="form-input">
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-outline" (click)="cancelar()">Cancelar</button>
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid || enviando">
              @if (enviando) {
                <span class="spinner-sm"></span>
                Registrando...
              } @else {
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
                  <polyline points="22,4 12,14.01 9,11.01"/>
                </svg>
                Registrar Solicitud
              }
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .page {
      padding: 2rem 3rem;
      min-height: calc(100vh - 60px);
      background: var(--color-bg, #f7f5f2);
    }

    .page-header {
      margin-bottom: 1.5rem;
    }

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
    }

    .btn-back:hover {
      color: var(--color-text, #3d3d3d);
      background: var(--color-bg-alt, #edeae5);
    }

    .form-container {
      max-width: 640px;
      margin: 0 auto;
    }

    .form-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .form-icon {
      width: 64px;
      height: 64px;
      background: var(--color-primary, #8b7355);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1rem;
      color: white;
    }

    .form-header h1 {
      font-size: 1.5rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      margin: 0 0 0.5rem 0;
    }

    .form-header p {
      color: var(--color-text-secondary, #6b6b6b);
      font-size: 0.9rem;
      margin: 0;
    }

    .alert {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem 1.25rem;
      border-radius: 10px;
      margin-bottom: 1.5rem;
      font-size: 0.9rem;
    }

    .alert-error {
      background: #fef2f2;
      color: #b91c1c;
      border: 1px solid #fecaca;
    }

    .alert-success {
      background: #f0fdf4;
      color: #15803d;
      border: 1px solid #bbf7d0;
    }

    .form-card {
      background: white;
      border-radius: 16px;
      padding: 2rem;
      box-shadow: var(--shadow-md, 0 4px 12px rgba(0,0,0,0.06));
    }

    .form-section {
      margin-bottom: 1.5rem;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
    }

    .form-label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      margin-bottom: 0.5rem;
    }

    .form-label svg {
      color: var(--color-primary, #8b7355);
      opacity: 0.7;
    }

    .optional {
      font-weight: 400;
      color: var(--color-text-muted, #9a9a9a);
    }

    .form-input {
      width: 100%;
      padding: 0.75rem 1rem;
      border: 1px solid var(--color-border, #e0dcd5);
      border-radius: 10px;
      font-size: 0.95rem;
      font-family: inherit;
      background: white;
      color: var(--color-text, #3d3d3d);
      transition: all 0.2s ease;
    }

    .form-input:focus {
      outline: none;
      border-color: var(--color-primary, #8b7355);
      box-shadow: 0 0 0 3px rgba(139, 115, 85, 0.1);
    }

    .form-input::placeholder {
      color: var(--color-text-muted, #9a9a9a);
    }

    .form-textarea {
      resize: vertical;
      min-height: 120px;
    }

    select.form-input {
      cursor: pointer;
    }

    .field-error {
      display: block;
      color: #dc2626;
      font-size: 0.8rem;
      margin-top: 0.4rem;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--color-border, #e0dcd5);
    }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.5rem;
      border-radius: 10px;
      font-size: 0.9rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
      border: none;
    }

    .btn-primary {
      background: var(--color-primary, #8b7355);
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background: var(--color-primary-hover, #6d5a44);
      transform: translateY(-1px);
    }

    .btn-primary:active:not(:disabled) {
      transform: scale(0.98);
    }

    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn-outline {
      background: transparent;
      color: var(--color-text-secondary, #6b6b6b);
      border: 1px solid var(--color-border, #e0dcd5);
    }

    .btn-outline:hover {
      border-color: var(--color-text-secondary, #6b6b6b);
      color: var(--color-text, #3d3d3d);
    }

    .spinner-sm {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255,255,255,0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    .spinner-xs {
      width: 14px;
      height: 14px;
      border: 2px solid var(--color-border, #e0dcd5);
      border-top-color: var(--color-primary, #8b7355);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .loading-select {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      background: var(--color-bg, #f7f5f2);
      border: 1px solid var(--color-border, #e0dcd5);
      border-radius: 10px;
      font-size: 0.9rem;
      color: var(--color-text-secondary, #6b6b6b);
    }

    .empty-select {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.75rem 1rem;
      background: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 10px;
      font-size: 0.9rem;
      color: #b91c1c;
    }

    .btn-retry {
      padding: 0.35rem 0.75rem;
      background: white;
      border: 1px solid #fecaca;
      border-radius: 6px;
      font-size: 0.8rem;
      color: #b91c1c;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-retry:hover {
      background: #fef2f2;
      border-color: #b91c1c;
    }

    .field-hint {
      display: block;
      font-size: 0.75rem;
      color: var(--color-text-muted, #9a9a9a);
      margin-top: 0.3rem;
    }

    .readonly-field {
      background: var(--color-bg-alt, #edeae5);
      cursor: not-allowed;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .badge-rol {
      font-size: 0.7rem;
      font-weight: 700;
      padding: 0.2rem 0.5rem;
      background: var(--color-primary, #8b7355);
      color: white;
      border-radius: 4px;
      text-transform: uppercase;
    }

    @media (max-width: 640px) {
      .page {
        padding: 1.5rem 1rem;
      }

      .form-card {
        padding: 1.5rem;
      }

      .form-row {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .form-actions {
        flex-direction: column-reverse;
      }

      .btn {
        width: 100%;
        justify-content: center;
      }
    }
  `]
})
export class SolicitudCreateComponent implements OnInit {
  solicitud: SolicitudRequest = {
    titulo: '',
    descripcion: '',
    canalOrigen: CanalOrigen.CSU,
    solicitanteId: 0,
    fechaLimite: ''
  };

  usuarios: UsuarioResponse[] = [];
  canales = Object.values(CanalOrigen);
  enviando = false;
  cargandoUsuarios = true;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    private solicitudService: SolicitudService,
    private usuarioService: UsuarioService,
    public authService: AuthService,
    private router: Router,
    private refreshService: DataRefreshService
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user && user.rol === 'ESTUDIANTE') {
      this.solicitud.solicitanteId = user.id;
      this.cargandoUsuarios = false;
    } else {
      this.cargarUsuarios();
    }
  }

  cargarUsuarios(): void {
    this.cargandoUsuarios = true;
    this.usuarioService.listarActivos().pipe(
      finalize(() => { this.cargandoUsuarios = false; })
    ).subscribe({
      next: res => {
        if (res.exitoso) {
          this.usuarios = res.datos;
        } else {
          console.warn('Error al cargar usuarios:', res.mensaje);
        }
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
      }
    });
  }

  enviar(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.enviando = true;

    const dto = { ...this.solicitud };
    if (!dto.fechaLimite) delete dto.fechaLimite;

    this.solicitudService.registrar(dto).pipe(
      finalize(() => { this.enviando = false; })
    ).subscribe({
      next: res => {
        if (res.exitoso) {
          this.mensajeExito = 'Solicitud registrada exitosamente';
          // Notificar que hay nuevas solicitudes
          this.refreshService.notifySolicitudesChanged();
          // Dar un poco más de tiempo para que los datos se persistan
          setTimeout(() => this.router.navigate(['/solicitudes']), 1200);
        } else {
          this.mensajeError = res.mensaje;
        }
      },
      error: err => {
        this.mensajeError = err.error?.mensaje || 'Error al registrar la solicitud';
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/solicitudes']);
  }

  canalLabel(c: CanalOrigen): string { return CANAL_LABELS[c] || c; }
  rolLabel(r: Rol): string { return ROL_LABELS[r] || r; }
}
