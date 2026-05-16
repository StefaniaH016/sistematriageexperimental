import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription, filter, finalize } from 'rxjs';
import { UsuarioService } from '../../services/usuario.service';
import {
  UsuarioResponse,
  UsuarioRequest,
  Rol,
  ROL_LABELS
} from '../../models';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuario-list.component.html',
  styleUrls: ['./usuario-list.component.css']
})
export class UsuarioListComponent implements OnInit, OnDestroy {
  usuarios: UsuarioResponse[] = [];
  cargando = true;
  mostrarFormulario = false;
  editandoId: number | null = null;
  mensaje = '';
  esError = false;
  roles = Object.values(Rol);

  formUsuario: UsuarioRequest = {
    identificacion: '',
    nombre: '',
    apellido: '',
    email: '',
    rol: Rol.ESTUDIANTE,
    password: ''
  };

  private routerSub?: Subscription;

  constructor(
    private usuarioService: UsuarioService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();
    
    // Recargar datos cada vez que se navega a esta ruta
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      if (event.urlAfterRedirects === '/usuarios') {
        this.cargarUsuarios();
      }
    });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  cargarUsuarios(): void {
    this.cargando = true;
    this.usuarioService.listarTodos().pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: res => {
        this.usuarios = res.exitoso ? res.datos : [];
        this.cdr.markForCheck();
      },
      error: (err) => { console.error('Error al cargar usuarios:', err); }
    });
  }

  guardar(): void {
    const obs = this.editandoId
      ? this.usuarioService.actualizar(this.editandoId, this.formUsuario)
      : this.usuarioService.crear(this.formUsuario);

    obs.subscribe({
      next: res => {
        if (res.exitoso) {
          this.show(this.editandoId ? 'Usuario actualizado' : 'Usuario creado exitosamente', false);
          this.cancelarForm();
          // Pequeño delay para permitir que Angular detecte cambios antes de cargar
          setTimeout(() => this.cargarUsuarios(), 100);
        } else {
          this.show(res.mensaje || 'Error al guardar', true);
        }
      },
      error: err => {
        this.show(err.error?.mensaje || 'Error al guardar', true);
        console.error('Error al guardar usuario:', err);
      }
    });
  }

  editar(u: UsuarioResponse): void {
    this.editandoId = u.id;
    this.formUsuario = {
      identificacion: u.identificacion,
      nombre: u.nombre,
      apellido: u.apellido,
      email: u.email,
      rol: u.rol,
      password: ''
    };
    this.mostrarFormulario = true;
  }

  desactivar(id: number): void {
    if (confirm('¿Desactivar este usuario?')) {
      this.usuarioService.desactivar(id).subscribe({
        next: () => {
          this.show('Usuario desactivado', false);
          setTimeout(() => this.cargarUsuarios(), 100);
        },
        error: err => {
          this.show(err.error?.mensaje || 'Error al desactivar', true);
          console.error('Error al desactivar usuario:', err);
        }
      });
    }
  }

  activar(id: number): void {
    if (confirm('¿Activar este usuario?')) {
      this.usuarioService.activar(id).subscribe({
        next: () => {
          this.show('Usuario activado', false);
          setTimeout(() => this.cargarUsuarios(), 100);
        },
        error: err => {
          this.show(err.error?.mensaje || 'Error al activar', true);
          console.error('Error al activar usuario:', err);
        }
      });
    }
  }

  cancelarForm(): void {
    this.mostrarFormulario = false;
    this.editandoId = null;
    this.formUsuario = {
      identificacion: '',
      nombre: '',
      apellido: '',
      email: '',
      rol: Rol.ESTUDIANTE,
      password: ''
    };
  }

  private show(msg: string, error: boolean): void {
    this.mensaje = msg;
    this.esError = error;
    setTimeout(() => this.mensaje = '', 4000);
  }

  rolLabel(r: Rol): string { return ROL_LABELS[r] || r; }
}
