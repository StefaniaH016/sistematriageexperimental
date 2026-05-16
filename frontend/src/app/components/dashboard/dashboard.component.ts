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
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
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
