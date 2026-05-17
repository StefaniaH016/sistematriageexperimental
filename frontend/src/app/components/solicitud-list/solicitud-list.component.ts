import { Component, OnInit, OnDestroy, ChangeDetectorRef, inject } from '@angular/core';
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
  templateUrl: './solicitud-list.component.html',
  styleUrls: ['./solicitud-list.component.css']
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

  private solicitudService = inject(SolicitudService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private refreshService = inject(DataRefreshService);
  public authService = inject(AuthService);

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

  /** Devuelve el observable correcto según el rol del usuario autenticado. */
  private obtenerFuenteDatos() {
    const rol = this.authService.getRol();
    if (rol === 'RESPONSABLE') {
      return this.solicitudService.listarPanelResponsable();
    }
    // ESTUDIANTE, DOCENTE → solo las propias; ADMINISTRATIVO → todas (backend lo decide)
    return this.solicitudService.listarMias();
  }

  cargarSolicitudes(): void {
    this.cargando = true;
    this.obtenerFuenteDatos().pipe(
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

    // Si no hay filtros activos, volver a la fuente correcta por rol
    if (!filtros.estado && !filtros.tipo && !filtros.prioridad) {
      this.obtenerFuenteDatos().pipe(
        finalize(() => { this.cargando = false; this.cdr.markForCheck(); })
      ).subscribe({
        next: res => { this.solicitudes = res.exitoso ? res.datos : []; this.cdr.markForCheck(); },
        error: (err) => { console.error('Error al cargar solicitudes:', err); }
      });
      return;
    }

    this.solicitudService.listar(filtros).pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: res => {
        // Filtrar client-side para respetar el alcance del rol
        const rol = this.authService.getRol();
        const userId = this.authService.getUserId();
        let datos: SolicitudResponse[] = res.exitoso ? res.datos : [];

        if (rol === 'ESTUDIANTE' || rol === 'DOCENTE') {
          datos = datos.filter(s => s.solicitante?.id === userId);
        } else if (rol === 'RESPONSABLE') {
          datos = datos.filter(s => !s.responsable || s.responsable?.id === userId);
        }

        this.solicitudes = datos;
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

  estadoLabel(e: any): string { return ESTADO_LABELS[e as EstadoSolicitud] || e; }
  tipoLabel(t: any): string {
    return TIPO_SOLICITUD_LABELS[t as TipoSolicitud] || t;
  }
  prioridadLabel(p: any): string { return PRIORIDAD_LABELS[p as Prioridad] || p; }
  canalLabel(c: any): string { return CANAL_LABELS[c as CanalOrigen] || c; }
}
