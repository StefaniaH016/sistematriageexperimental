import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription, filter, finalize } from 'rxjs';
import { SolicitudService } from '../../services/solicitud.service';
import { DataRefreshService } from '../../services/data-refresh.service';
import { AuthService } from '../../services/auth.service';
import {
  SolicitudResponse,
  EstadoSolicitud,
  TipoSolicitud,
  Prioridad,
  ESTADO_LABELS,
  PRIORIDAD_LABELS,
  TIPO_SOLICITUD_LABELS,
  CANAL_LABELS,
  CanalOrigen
} from '../../models';

@Component({
  selector: 'app-solicitud-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">Solicitudes</h1>
          <p class="page-subtitle">Gestiona y da seguimiento a las solicitudes académicas</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-refresh" (click)="cargarSolicitudes()" [disabled]="cargando" title="Actualizar lista">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" [class.spinning]="cargando">
              <path d="M23 4v6h-6"/>
              <path d="M1 20v-6h6"/>
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
            </svg>
            {{ cargando ? 'Actualizando...' : 'Actualizar' }}
          </button>
        </div>
      </div>

      <!-- Filters -->
      <div class="filters-card">
        <div class="filters-row">
          <div class="filter-group">
            <label>Estado</label>
            <select [(ngModel)]="filtroEstado" (change)="aplicarFiltros()">
              <option value="">Todos los estados</option>
              @for (e of estados; track e) {
                <option [value]="e">{{ estadoLabel(e) }}</option>
              }
            </select>
          </div>
          <div class="filter-group">
            <label>Tipo</label>
            <select [(ngModel)]="filtroTipo" (change)="aplicarFiltros()">
              <option value="">Todos los tipos</option>
              @for (t of tipos; track t) {
                <option [value]="t">{{ tipoLabel(t) }}</option>
              }
            </select>
          </div>
          <div class="filter-group">
            <label>Prioridad</label>
            <select [(ngModel)]="filtroPrioridad" (change)="aplicarFiltros()">
              <option value="">Todas las prioridades</option>
              @for (p of prioridades; track p) {
                <option [value]="p">{{ prioridadLabel(p) }}</option>
              }
            </select>
          </div>
          <button class="btn btn-outline" (click)="limpiarFiltros()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 6h18M6 6v14a2 2 0 002 2h8a2 2 0 002-2V6"/>
            </svg>
            Limpiar
          </button>
        </div>
      </div>

      <!-- Cards List -->
      <div class="solicitudes-container">
        @if (cargando) {
          <div class="loading-container">
            <div class="spinner"></div>
            <span>Cargando solicitudes...</span>
          </div>
        } @else if (solicitudes.length === 0) {
          <div class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/>
              <rect x="9" y="3" width="6" height="4" rx="1"/>
            </svg>
            <h3>No hay solicitudes</h3>
            @if (authService.currentUserValue?.rol === 'ESTUDIANTE') {
              <p>Crea una nueva solicitud para comenzar</p>
            }
          </div>
        } @else {
          <div class="solicitudes-list">
            @for (s of solicitudes; track s.id) {
              <a [routerLink]="['/solicitudes', s.id]" class="solicitud-card">
                <div class="card-header">
                  <span class="card-id">#{{ s.id }}</span>
                  <div class="card-badges">
                    <span class="badge" [class]="'badge-estado-' + s.estado.toLowerCase()">
                      {{ estadoLabel(s.estado) }}
                    </span>
                    @if (s.prioridad) {
                      <span class="badge" [class]="'badge-prioridad-' + s.prioridad.toLowerCase()">
                        {{ prioridadLabel(s.prioridad) }}
                      </span>
                    }
                  </div>
                </div>
                <h3 class="card-title">{{ s.titulo }}</h3>
                <div class="card-meta">
                  @if (s.tipo) {
                    <span class="meta-item">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                        <line x1="7" y1="7" x2="7.01" y2="7"/>
                      </svg>
                      {{ tipoLabel(s.tipo) }}
                    </span>
                  }
                  <span class="meta-item">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    {{ s.solicitante.nombre }} {{ s.solicitante.apellido }}
                  </span>
                  <span class="meta-item">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                      <line x1="16" y1="2" x2="16" y2="6"/>
                      <line x1="8" y1="2" x2="8" y2="6"/>
                      <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    {{ s.fechaCreacion | date:'dd/MM/yyyy' }}
                  </span>
                </div>
                <div class="card-footer">
                  <span class="canal-badge">{{ canalLabel(s.canalOrigen) }}</span>
                  <svg class="arrow-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 18l6-6-6-6"/>
                  </svg>
                </div>
              </a>
            }
          </div>
          <div class="list-footer">
            <span class="total-count">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/>
              </svg>
              Total: <strong>{{ solicitudes.length }}</strong> solicitudes
            </span>
          </div>
        }
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
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .header-actions {
      display: flex;
      gap: 0.75rem;
      align-items: center;
    }

    .header-content h1 {
      font-size: 1.75rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      margin: 0 0 0.25rem 0;
    }

    .page-subtitle {
      color: var(--color-text-secondary, #6b6b6b);
      font-size: 0.9rem;
      margin: 0;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.7rem 1.25rem;
      border-radius: 10px;
      font-size: 0.9rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
      text-decoration: none;
      border: none;
    }

    .btn-refresh {
      background: var(--color-card, #ffffff);
      color: var(--color-text-secondary, #6b6b6b);
      border: 1px solid var(--color-border, #e0dcd5);
    }

    .btn-refresh:hover:not(:disabled) {
      border-color: var(--color-primary, #8b7355);
      color: var(--color-primary, #8b7355);
      background: rgba(139, 115, 85, 0.05);
    }

    .btn-refresh:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn-refresh svg.spinning {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .btn-primary {
      background: var(--color-primary, #8b7355);
      color: white;
    }

    .btn-primary:hover {
      background: var(--color-primary-hover, #6d5a44);
      transform: translateY(-1px);
    }

    .btn-primary:active {
      transform: scale(0.98);
    }

    .btn-outline {
      background: transparent;
      color: var(--color-text-secondary, #6b6b6b);
      border: 1px solid var(--color-border, #e0dcd5);
    }

    .btn-outline:hover {
      border-color: var(--color-primary, #8b7355);
      color: var(--color-primary, #8b7355);
      background: rgba(139, 115, 85, 0.05);
    }

    .filters-card {
      background: white;
      border-radius: 12px;
      padding: 1.25rem 1.5rem;
      margin-bottom: 1.5rem;
      box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.04));
    }

    .filters-row {
      display: flex;
      gap: 1rem;
      align-items: flex-end;
      flex-wrap: wrap;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      flex: 1;
      min-width: 180px;
    }

    .filter-group label {
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--color-text-secondary, #6b6b6b);
    }

    .filter-group select {
      padding: 0.6rem 0.75rem;
      border: 1px solid var(--color-border, #e0dcd5);
      border-radius: 8px;
      font-size: 0.9rem;
      background: white;
      color: var(--color-text, #3d3d3d);
      cursor: pointer;
      transition: border-color 0.2s ease;
    }

    .filter-group select:focus {
      outline: none;
      border-color: var(--color-primary, #8b7355);
    }

    .solicitudes-container {
      min-height: 300px;
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

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

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
    }

    .empty-state svg {
      opacity: 0.4;
      margin-bottom: 1rem;
    }

    .empty-state h3 {
      margin: 0 0 0.5rem 0;
      color: var(--color-text, #3d3d3d);
    }

    .empty-state p {
      margin: 0;
      font-size: 0.9rem;
    }

    .solicitudes-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .solicitud-card {
      background: white;
      border-radius: 12px;
      padding: 1.25rem 1.5rem;
      text-decoration: none;
      color: inherit;
      transition: all 0.2s ease;
      box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.04));
      border: 1px solid transparent;
    }

    .solicitud-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md, 0 4px 12px rgba(0,0,0,0.06));
      border-color: var(--color-primary-light, #d4c8b8);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
    }

    .card-id {
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--color-primary, #8b7355);
    }

    .card-badges {
      display: flex;
      gap: 0.5rem;
    }

    .badge {
      padding: 0.25rem 0.6rem;
      font-size: 0.7rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      border-radius: 6px;
    }

    .badge-estado-registrada { background: #e3f2fd; color: #1565c0; }
    .badge-estado-clasificada { background: #fff3e0; color: #e65100; }
    .badge-estado-en_atencion { background: #e8f5e9; color: #2e7d32; }
    .badge-estado-atendida { background: #f3e5f5; color: #7b1fa2; }
    .badge-estado-cerrada { background: var(--color-bg-alt, #edeae5); color: var(--color-text-secondary, #6b6b6b); }

    .badge-prioridad-baja { background: #e8f5e9; color: #388e3c; }
    .badge-prioridad-media { background: #fff8e1; color: #f9a825; }
    .badge-prioridad-alta { background: #ffebee; color: #c62828; }
    .badge-prioridad-critica { background: #c62828; color: white; }

    .card-title {
      font-size: 1rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      margin: 0 0 0.75rem 0;
      line-height: 1.4;
    }

    .card-meta {
      display: flex;
      gap: 1.25rem;
      flex-wrap: wrap;
      margin-bottom: 0.75rem;
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 0.8rem;
      color: var(--color-text-secondary, #6b6b6b);
    }

    .meta-item svg {
      opacity: 0.6;
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 0.75rem;
      border-top: 1px solid var(--color-border, #e0dcd5);
    }

    .canal-badge {
      font-size: 0.75rem;
      color: var(--color-text-muted, #9a9a9a);
    }

    .arrow-icon {
      color: var(--color-primary, #8b7355);
      opacity: 0;
      transform: translateX(-5px);
      transition: all 0.2s ease;
    }

    .solicitud-card:hover .arrow-icon {
      opacity: 1;
      transform: translateX(0);
    }

    .list-footer {
      padding: 1.25rem 0;
      margin-top: 1rem;
    }

    .total-count {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.85rem;
      color: var(--color-text-secondary, #6b6b6b);
    }

    @media (max-width: 768px) {
      .page {
        padding: 1.5rem 1rem;
      }

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .filters-row {
        flex-direction: column;
      }

      .filter-group {
        width: 100%;
      }

      .card-meta {
        flex-direction: column;
        gap: 0.5rem;
      }
    }
  `]
})
export class SolicitudListComponent implements OnInit, OnDestroy {
  solicitudes: SolicitudResponse[] = [];
  cargando = true;
  estados = Object.values(EstadoSolicitud);
  tipos = Object.values(TipoSolicitud);
  prioridades = Object.values(Prioridad);

  filtroEstado = '';
  filtroTipo = '';
  filtroPrioridad = '';

  private routerSub?: Subscription;
  private refreshSub?: Subscription;

  constructor(
    private solicitudService: SolicitudService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private refreshService: DataRefreshService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.cargarSolicitudes();
    
    // Recargar datos cada vez que se navega A esta ruta
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      if (event.urlAfterRedirects === '/solicitudes') {
        this.cargarSolicitudes();
      }
    });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  cargarSolicitudes(): void {
    this.cargando = true;
    this.solicitudService.listarTodas().pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: res => {
        this.solicitudes = res.exitoso ? res.datos : [];
        this.cdr.markForCheck();
      },
      error: (err) => { console.error('Error al cargar solicitudes:', err); }
    });
  }

  aplicarFiltros(): void {
    this.cargando = true;
    const filtros: any = {};
    if (this.filtroEstado) filtros.estado = this.filtroEstado;
    if (this.filtroTipo) filtros.tipo = this.filtroTipo;
    if (this.filtroPrioridad) filtros.prioridad = this.filtroPrioridad;

    this.solicitudService.listar(filtros).pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: res => {
        this.solicitudes = res.exitoso ? res.datos : [];
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error al aplicar filtros:', err);
      }
    });
  }

  limpiarFiltros(): void {
    this.filtroEstado = '';
    this.filtroTipo = '';
    this.filtroPrioridad = '';
    this.cargarSolicitudes();
  }

  estadoLabel(e: EstadoSolicitud): string { return ESTADO_LABELS[e] || e; }
  tipoLabel(t: TipoSolicitud): string { return TIPO_SOLICITUD_LABELS[t] || t; }
  prioridadLabel(p: Prioridad): string { return PRIORIDAD_LABELS[p] || p; }
  canalLabel(c: CanalOrigen): string { return CANAL_LABELS[c] || c; }
}
