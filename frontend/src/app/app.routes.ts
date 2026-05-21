import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SolicitudListComponent } from './components/solicitud-list/solicitud-list.component';
import { SolicitudCreateComponent } from './components/solicitud-create/solicitud-create.component';
import { SolicitudDetailComponent } from './components/solicitud-detail/solicitud-detail.component';
import { UsuarioListComponent } from './components/usuario-list/usuario-list.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: AuthComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'solicitudes', component: SolicitudListComponent, canActivate: [authGuard] },
  { path: 'solicitudes/nueva', component: SolicitudCreateComponent, canActivate: [authGuard] },
  { path: 'solicitudes/:id', component: SolicitudDetailComponent, canActivate: [authGuard] },
  { path: 'usuarios', component: UsuarioListComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' }
];
