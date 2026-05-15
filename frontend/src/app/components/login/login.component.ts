import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  isLogin = true;
  credenciales = { 
    identificacion: '',
    nombre: '',
    apellido: '',
    email: '', 
    password: '',
    rol: 'ESTUDIANTE'
  };
  errorMensaje = '';
  exitoMensaje = '';

  constructor(private authService: AuthService, private router: Router) { }

  toggleMode() {
    this.isLogin = !this.isLogin;
    this.errorMensaje = '';
    this.exitoMensaje = '';
  }

  submit() {
    this.errorMensaje = '';
    this.exitoMensaje = '';
    if (this.isLogin) {
      this.authService.login(this.credenciales.email, this.credenciales.password).subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: err => {
          this.errorMensaje = 'Credenciales inválidas o error en el servidor.';
          console.error(err);
        }
      });
    } else {
      this.authService.register(this.credenciales).subscribe({
        next: () => {
          this.exitoMensaje = '¡Cuenta creada exitosamente! Ahora puedes iniciar sesión.';
          this.isLogin = true;
        },
        error: err => {
          this.errorMensaje = err.error?.mensaje || 'Hubo un error al crear la cuenta. Verifica tus datos.';
          console.error(err);
        }
      });
    }
  }
}
