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
  templateUrl: './solicitud-detail.component.html',
  styleUrls: ['./solicitud-detail.component.css']
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
