import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { Subscription, filter, finalize } from 'rxjs';
import { SolicitudService } from '../../services/solicitud.service';
import { DataRefreshService } from '../../services/data-refresh.service';
import { AuthService } from '../../services/auth.service';
import {
  SolicitudResponse,
  EstadoSolicitud,
  Prioridad,
  ESTADO_LABELS,
  PRIORIDAD_LABELS,
  TIPO_SOLICITUD_LABELS
} from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard">
      <header class="page-header">
        <div>
          <h1 class="page-title">Dashboard</h1>
          <p class="page-subtitle">Resumen del sistema de solicitudes académicas</p>
        </div>
        <div class="header-actions">
          <button class="btn-refresh" (click)="cargar()" [disabled]="cargando" title="Actualizar datos">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" [class.spinning]="cargando">
              <path d="M23 4v6h-6"/>
              <path d="M1 20v-6h6"/>
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
            </svg>
            {{ cargando ? 'Actualizando...' : 'Actualizar' }}
          </button>
        </div>
      </header>

      <!-- Stats Cards -->
      <div class="stats-grid">
        <div class="stat-card" (click)="null">
          <div class="stat-icon registrada">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
              <polyline points="7,10 12,15 17,10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ contarPorEstado('REGISTRADA') }}</div>
            <div class="stat-label">Registradas</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon clasificada">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
              <line x1="7" y1="7" x2="7.01" y2="7"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ contarPorEstado('CLASIFICADA') }}</div>
            <div class="stat-label">Clasificadas</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon en-atencion">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12,6 12,12 16,14"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ contarPorEstado('EN_ATENCION') }}</div>
            <div class="stat-label">En Atención</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon atendida">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
              <polyline points="22,4 12,14.01 9,11.01"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ contarPorEstado('ATENDIDA') }}</div>
            <div class="stat-label">Atendidas</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon cerrada">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0110 0v4"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ contarPorEstado('CERRADA') }}</div>
            <div class="stat-label">Cerradas</div>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="content-grid">
        <!-- Priority Distribution -->
        <div class="card">
          <div class="card-header">
            <h2>Distribución por Prioridad</h2>
          </div>
          <div class="card-body">
            <div class="priority-list">
              @for (p of prioridades; track p) {
                <div class="priority-item">
                  <div class="priority-info">
                    <span class="priority-dot" [class]="'dot-' + p.toLowerCase()"></span>
                    <span class="priority-name">{{ prioridadLabel(p) }}</span>
                  </div>
                  <div class="priority-bar-container">
                    <div class="priority-bar" [class]="'bar-' + p.toLowerCase()"
                      [style.width.%]="getBarWidth(p)">
                    </div>
                  </div>
                  <span class="priority-count">{{ contarPorPrioridad(p) }}</span>
                </div>
              }
            </div>
            
            @if (solicitudes.length === 0) {
              <div class="empty-state">
                <p>No hay datos disponibles</p>
              </div>
            }
          </div>
        </div>

        <!-- Recent Requests -->
        <div class="card">
          <div class="card-header">
            <h2>Solicitudes Recientes</h2>
            <a routerLink="/solicitudes" class="view-all">Ver todas →</a>
          </div>
          <div class="card-body">
            @if (solicitudes.length === 0) {
              <div class="empty-state">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/>
                  <rect x="9" y="3" width="6" height="4" rx="1"/>
                </svg>
                <p>No hay solicitudes registradas</p>
                @if (authService.currentUserValue?.rol === 'ESTUDIANTE') {
                  <a routerLink="/solicitudes/nueva" class="btn-link">Crear primera solicitud</a>
                }
              </div>
            } @else {
              <div class="recent-list">
                @for (s of ultimasSolicitudes; track s.id) {
                  <a [routerLink]="['/solicitudes', s.id]" class="recent-item">
                    <div class="recent-main">
                      <span class="recent-id">#{{ s.id }}</span>
                      <span class="recent-title">{{ s.titulo }}</span>
                    </div>
                    <div class="recent-details">
                      <span class="badge" [class]="'badge-' + s.estado.toLowerCase()">
                        {{ estadoLabel(s.estado) }}
                      </span>
                      <span class="recent-date">{{ s.fechaCreacion | date:'dd/MM/yy' }}</span>
                    </div>
                  </a>
                }
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard {
      padding: 2rem;
      max-width: 1400px;
      margin: 0 auto;
      animation: fadeIn 0.4s ease-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(12px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .loading-overlay {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1.25rem;
      background: var(--color-card, #ffffff);
      border: 1px solid var(--color-border, #e0dcd5);
      border-radius: var(--radius-md, 10px);
      margin-bottom: 1.5rem;
      font-size: 0.9rem;
      color: var(--color-text-secondary, #6b6b6b);
    }

    .spinner {
      width: 20px;
      height: 20px;
      border: 2px solid var(--color-border, #e0dcd5);
      border-top-color: var(--color-primary, #8b7355);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;
    }

    .header-actions {
      display: flex;
      gap: 0.75rem;
      align-items: center;
    }

    .page-title {
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--color-text, #3d3d3d);
      margin-bottom: 0.25rem;
      letter-spacing: -0.02em;
    }

    .page-subtitle {
      color: var(--color-text-secondary, #6b6b6b);
      font-size: 0.95rem;
    }

    .btn-refresh {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.25rem;
      background: var(--color-card, #ffffff);
      color: var(--color-text-secondary, #6b6b6b);
      border: 1px solid var(--color-border, #e0dcd5);
      border-radius: var(--radius-md, 10px);
      font-weight: 500;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.2s ease;
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

    .btn-primary {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.25rem;
      background: var(--color-primary, #8b7355);
      color: white;
      border: none;
      border-radius: var(--radius-md, 10px);
      font-weight: 600;
      font-size: 0.9rem;
      text-decoration: none;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-primary:hover {
      background: var(--color-primary-hover, #6d5a44);
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(139, 115, 85, 0.25);
    }

    .btn-primary:active {
      transform: translateY(0);
    }

    /* Stats Grid */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      background: var(--color-card, #ffffff);
      border-radius: var(--radius-md, 10px);
      padding: 1.25rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      border: 1px solid var(--color-border, #e0dcd5);
      transition: all 0.2s ease;
    }

    .stat-card:hover {
      border-color: var(--color-primary-light, #d4c8b8);
      box-shadow: var(--shadow-md, 0 4px 12px rgba(0,0,0,0.06));
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-sm, 6px);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .stat-icon.registrada { background: #e8f4fd; color: #3b82b6; }
    .stat-icon.clasificada { background: #f3e8fd; color: #8b5cf6; }
    .stat-icon.en-atencion { background: #fef3e2; color: #d97706; }
    .stat-icon.atendida { background: #e8f5e9; color: #22863a; }
    .stat-icon.cerrada { background: var(--color-bg-alt, #edeae5); color: var(--color-text-secondary, #6b6b6b); }

    .stat-content {
      flex: 1;
      min-width: 0;
    }

    .stat-number {
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--color-text, #3d3d3d);
      line-height: 1.2;
    }

    .stat-label {
      font-size: 0.85rem;
      color: var(--color-text-secondary, #6b6b6b);
      margin-top: 0.15rem;
    }

    /* Content Grid */
    .content-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
    }

    .card {
      background: var(--color-card, #ffffff);
      border-radius: var(--radius-lg, 16px);
      border: 1px solid var(--color-border, #e0dcd5);
      overflow: hidden;
    }

    .card-header {
      padding: 1.25rem 1.5rem;
      border-bottom: 1px solid var(--color-border, #e0dcd5);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-header h2 {
      font-size: 1rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
    }

    .view-all {
      font-size: 0.85rem;
      color: var(--color-primary, #8b7355);
      text-decoration: none;
      font-weight: 500;
      transition: color 0.2s;
    }

    .view-all:hover {
      color: var(--color-primary-hover, #6d5a44);
    }

    .card-body {
      padding: 1.5rem;
    }

    /* Priority List */
    .priority-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .priority-item {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .priority-info {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      min-width: 80px;
    }

    .priority-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
    }

    .dot-baja { background: var(--color-success, #5d8a66); }
    .dot-media { background: var(--color-info, #5a7d9a); }
    .dot-alta { background: var(--color-warning, #c9a227); }
    .dot-critica { background: var(--color-danger, #b85450); }

    .priority-name {
      font-size: 0.9rem;
      color: var(--color-text-secondary, #6b6b6b);
      text-transform: capitalize;
    }

    .priority-bar-container {
      flex: 1;
      height: 8px;
      background: var(--color-bg-alt, #edeae5);
      border-radius: 4px;
      overflow: hidden;
    }

    .priority-bar {
      height: 100%;
      border-radius: 4px;
      transition: width 0.6s ease-out;
      min-width: 4px;
    }

    .bar-baja { background: var(--color-success, #5d8a66); }
    .bar-media { background: var(--color-info, #5a7d9a); }
    .bar-alta { background: var(--color-warning, #c9a227); }
    .bar-critica { background: var(--color-danger, #b85450); }

    .priority-count {
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
      min-width: 28px;
      text-align: right;
      font-size: 0.9rem;
    }

    /* Recent List */
    .recent-list {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .recent-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.85rem 1rem;
      border-radius: var(--radius-sm, 6px);
      text-decoration: none;
      color: inherit;
      background: var(--color-bg, #f7f5f2);
      transition: all 0.2s ease;
    }

    .recent-item:hover {
      background: var(--color-bg-alt, #edeae5);
      transform: translateX(4px);
    }

    .recent-main {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      min-width: 0;
      flex: 1;
    }

    .recent-id {
      font-size: 0.8rem;
      color: var(--color-text-muted, #9a9a9a);
      font-weight: 500;
    }

    .recent-title {
      font-weight: 500;
      font-size: 0.9rem;
      color: var(--color-text, #3d3d3d);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .recent-details {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      flex-shrink: 0;
    }

    .recent-date {
      font-size: 0.8rem;
      color: var(--color-text-muted, #9a9a9a);
    }

    /* Badges */
    .badge {
      display: inline-block;
      padding: 0.25rem 0.6rem;
      border-radius: 20px;
      font-size: 0.7rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }

    .badge-registrada { background: #e8f4fd; color: #3b82b6; }
    .badge-clasificada { background: #f3e8fd; color: #8b5cf6; }
    .badge-en_atencion { background: #fef3e2; color: #d97706; }
    .badge-atendida { background: #e8f5e9; color: #22863a; }
    .badge-cerrada { background: var(--color-bg-alt, #edeae5); color: var(--color-text-secondary, #6b6b6b); }

    /* Empty State */
    .empty-state {
      text-align: center;
      padding: 2.5rem 1rem;
      color: var(--color-text-muted, #9a9a9a);
    }

    .empty-state svg {
      margin-bottom: 1rem;
      opacity: 0.5;
    }

    .empty-state p {
      margin-bottom: 0.75rem;
    }

    .btn-link {
      color: var(--color-primary, #8b7355);
      text-decoration: none;
      font-weight: 500;
      font-size: 0.9rem;
    }

    .btn-link:hover {
      text-decoration: underline;
    }

    /* Responsive */
    @media (max-width: 1200px) {
      .stats-grid {
        grid-template-columns: repeat(3, 1fr);
      }
    }

    @media (max-width: 900px) {
      .content-grid {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 768px) {
      .dashboard {
        padding: 1rem;
      }

      .page-header {
        flex-direction: column;
        gap: 1rem;
      }

      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
      }

      .stat-card {
        padding: 1rem;
      }

      .stat-icon {
        width: 40px;
        height: 40px;
      }

      .stat-number {
        font-size: 1.5rem;
      }
    }

    @media (max-width: 480px) {
      .stats-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  solicitudes: SolicitudResponse[] = [];
  prioridades = Object.values(Prioridad);
  cargando = true;

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
    this.cargar();
    
    // Recargar datos cada vez que se navega al dashboard
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      if (event.urlAfterRedirects === '/dashboard' || event.urlAfterRedirects === '/solicitudes') {
        this.cargar();
      }
    });
    
    // Recargar cuando hay cambios de solicitudes desde otros componentes
    this.refreshSub = this.refreshService.getSolicitudesRefresh().subscribe(() => {
      this.cargar();
    });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
    this.refreshSub?.unsubscribe();
  }

  cargar(): void {
    this.cargando = true;
    this.solicitudService.listarTodas().pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: res => {
        if (res.exitoso) {
          this.solicitudes = res.datos;
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error al cargar dashboard:', err);
      }
    });
  }

  contarPorEstado(estado: string): number {
    return this.solicitudes.filter(s => s.estado === estado).length;
  }

  contarPorPrioridad(prioridad: string): number {
    return this.solicitudes.filter(s => s.prioridad === prioridad).length;
  }

  getBarWidth(prioridad: string): number {
    const total = this.solicitudes.length || 1;
    return (this.contarPorPrioridad(prioridad) / total) * 100;
  }

  get ultimasSolicitudes(): SolicitudResponse[] {
    return [...this.solicitudes]
      .sort((a, b) => new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime())
      .slice(0, 5);
  }

  estadoLabel(e: EstadoSolicitud): string { return ESTADO_LABELS[e] || e; }
  prioridadLabel(p: string): string { return PRIORIDAD_LABELS[p as Prioridad] || p; }
  tipoLabel(t: any): string { return TIPO_SOLICITUD_LABELS[t as keyof typeof TIPO_SOLICITUD_LABELS] || t; }
}
