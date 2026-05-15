import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  template: `
    <nav class="navbar">
      <div class="navbar-brand">
        <a routerLink="/dashboard" class="brand-link">
          <div class="brand-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
              <path d="M6 12v5c3 3 9 3 12 0v-5"/>
            </svg>
          </div>
          <span class="brand-text">Triage Académico</span>
        </a>
      </div>
      
      <div class="navbar-menu">
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="9" rx="1"/>
            <rect x="14" y="3" width="7" height="5" rx="1"/>
            <rect x="14" y="12" width="7" height="9" rx="1"/>
            <rect x="3" y="16" width="7" height="5" rx="1"/>
          </svg>
          <span>Dashboard</span>
        </a>
        <a routerLink="/solicitudes" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" class="nav-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/>
            <rect x="9" y="3" width="6" height="4" rx="1"/>
            <path d="M9 12h6M9 16h6"/>
          </svg>
          <span>Solicitudes</span>
        </a>
        @if (authService.currentUserValue?.rol === 'ADMINISTRATIVO') {
          <a routerLink="/usuarios" routerLinkActive="active" class="nav-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
            </svg>
            <span>Usuarios</span>
          </a>
        }
      </div>

      <div class="navbar-actions">
        @if (authService.currentUserValue?.rol === 'ESTUDIANTE') {
          <a routerLink="/solicitudes/nueva" class="nav-link nav-link-accent mr-2">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 8v8M8 12h8"/>
            </svg>
            <span>Nueva Solicitud</span>
          </a>
        }
        <div class="user-profile">
          <div class="user-info">
            <span class="user-name">{{ authService.currentUserValue?.nombre }}</span>
            <span class="user-role">{{ authService.currentUserValue?.rol }}</span>
          </div>
        </div>
        <button class="btn-logout" (click)="logout()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
            <polyline points="16,17 21,12 16,7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          <span>Salir</span>
        </button>
      </div>
    </nav>
  `,
  styles: [`
    .user-profile {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-right: 1.5rem;
      padding-right: 1.5rem;
      border-right: 1px solid var(--color-border, #e0dcd5);
    }

    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }

    .user-name {
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--color-text, #3d3d3d);
    }

    .user-role {
      font-size: 0.7rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--color-primary, #8b7355);
    }
    .mr-2 {
      margin-right: 0.75rem;
    }
    .navbar {
      background: #ffffff;
      padding: 0.75rem 2rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid var(--color-border, #e0dcd5);
      position: sticky;
      top: 0;
      z-index: 100;
      box-shadow: 0 1px 3px rgba(0,0,0,0.03);
    }

    .brand-link {
      text-decoration: none;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      color: var(--color-text, #3d3d3d);
      transition: opacity 0.2s ease;
    }

    .brand-link:hover {
      opacity: 0.8;
    }

    .brand-icon {
      width: 36px;
      height: 36px;
      background: var(--color-bg-alt, #edeae5);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--color-primary, #8b7355);
    }

    .brand-text {
      font-size: 1.1rem;
      font-weight: 600;
      letter-spacing: -0.02em;
    }

    .navbar-menu {
      display: flex;
      gap: 0.5rem;
    }

    .nav-link {
      color: var(--color-text-secondary, #6b6b6b);
      text-decoration: none;
      padding: 0.6rem 1rem;
      font-size: 0.9rem;
      font-weight: 500;
      transition: all 0.2s ease;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      border-radius: 8px;
      position: relative;
    }

    .nav-link svg {
      opacity: 0.7;
      transition: opacity 0.2s ease;
    }

    .nav-link:hover {
      color: var(--color-text, #3d3d3d);
      background: var(--color-bg-alt, #edeae5);
    }

    .nav-link:hover svg {
      opacity: 1;
    }

    .nav-link.active {
      color: var(--color-primary, #8b7355);
      background: rgba(139, 115, 85, 0.08);
    }

    .nav-link.active svg {
      opacity: 1;
    }

    .nav-link-accent {
      background: var(--color-primary, #8b7355);
      color: white !important;
    }

    .nav-link-accent:hover {
      background: var(--color-primary-hover, #6d5a44) !important;
      color: white !important;
    }

    .nav-link-accent svg {
      opacity: 1 !important;
    }

    .navbar-actions {
      display: flex;
      align-items: center;
    }

    .btn-logout {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      border: 1px solid var(--color-border, #e0dcd5);
      background: transparent;
      color: var(--color-text-secondary, #6b6b6b);
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-logout:hover {
      border-color: var(--color-danger, #b85450);
      color: var(--color-danger, #b85450);
      background: rgba(184, 84, 80, 0.05);
    }

    .btn-logout:active {
      transform: scale(0.97);
    }

    @media (max-width: 768px) {
      .navbar {
        padding: 0.75rem 1rem;
        flex-wrap: wrap;
        gap: 0.75rem;
      }
      
      .navbar-menu {
        order: 3;
        width: 100%;
        justify-content: center;
        flex-wrap: wrap;
      }

      .nav-link span {
        display: none;
      }

      .brand-text {
        font-size: 0.95rem;
      }
    }
  `]
})
export class NavbarComponent {
  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
