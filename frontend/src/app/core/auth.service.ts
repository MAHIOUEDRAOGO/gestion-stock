import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { LoginResponse, Role, Utilisateur } from './models';

const CLE_TOKEN = 'gs_token';
const CLE_UTILISATEUR = 'gs_utilisateur';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _utilisateur = signal<Utilisateur | null>(lireUtilisateur());

  readonly utilisateur = this._utilisateur.asReadonly();
  readonly connecte = computed(() => this._utilisateur() !== null && this.token() !== null);

  login(login: string, motDePasse: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { login, motDePasse }).pipe(
      tap((r) => {
        sessionStorage.setItem(CLE_TOKEN, r.token);
        sessionStorage.setItem(CLE_UTILISATEUR, JSON.stringify(r.utilisateur));
        this._utilisateur.set(r.utilisateur);
      }),
    );
  }

  logout(): void {
    sessionStorage.removeItem(CLE_TOKEN);
    sessionStorage.removeItem(CLE_UTILISATEUR);
    this._utilisateur.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return sessionStorage.getItem(CLE_TOKEN);
  }

  aRole(...roles: Role[]): boolean {
    const u = this._utilisateur();
    return !!u && roles.includes(u.role);
  }
}

function lireUtilisateur(): Utilisateur | null {
  const brut = sessionStorage.getItem(CLE_UTILISATEUR);
  return brut ? (JSON.parse(brut) as Utilisateur) : null;
}
