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
  templateUrl: './solicitud-create.component.html',
  styleUrls: ['./solicitud-create.component.css']
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
