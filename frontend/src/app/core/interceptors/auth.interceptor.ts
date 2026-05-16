import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';

/**
 * Interceptor único para manejar seguridad JWT y errores de autorización.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  let authReq = req;
  
  // Añadir token si existe y la petición va hacia nuestra API
  if (token && (req.url.includes('/api/') || req.url.startsWith('/api/'))) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthEndpoint = req.url.includes('/api/auth/');
      
      // Si recibimos un 401 o 403 fuera de login, el token ya no es válido
      if ((error.status === 401 || error.status === 403) && !isAuthEndpoint) {
        console.error('Sesión inválida o expirada. Redirigiendo...');
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
