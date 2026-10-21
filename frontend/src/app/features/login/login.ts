import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { messageErreur } from '../../core/auth.interceptor';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  template: `
    <div class="login-page">
      <form class="card login-card" (ngSubmit)="valider()">
        <h1>Gestion de stock</h1>
        <p class="muted">Connectez-vous pour continuer</p>
        <label>Identifiant
          <input name="login" [(ngModel)]="login" required autofocus autocomplete="username" />
        </label>
        <label>Mot de passe
          <input name="mdp" type="password" [(ngModel)]="motDePasse" required autocomplete="current-password" />
        </label>
        @if (erreur()) {
          <p class="error">{{ erreur() }}</p>
        }
        <button class="btn" type="submit" [disabled]="chargement() || !login || !motDePasse">
          {{ chargement() ? 'Connexion…' : 'Se connecter' }}
        </button>
      </form>
    </div>
  `,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected login = '';
  protected motDePasse = '';
  protected readonly erreur = signal('');
  protected readonly chargement = signal(false);

  valider(): void {
    this.chargement.set(true);
    this.erreur.set('');
    this.auth.login(this.login, this.motDePasse).subscribe({
      next: () => this.router.navigate(['/']),
      error: (e) => {
        this.erreur.set(messageErreur(e));
        this.chargement.set(false);
      },
    });
  }
}
