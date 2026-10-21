import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { UtilisateurApi } from '../../core/api';
import { messageErreur } from '../../core/auth.interceptor';
import { Role, ROLE_LIBELLES, Utilisateur } from '../../core/models';

@Component({
  selector: 'app-utilisateurs',
  imports: [FormsModule],
  template: `
    <h1>Utilisateurs</h1>

    <form class="card" (ngSubmit)="creer()" #f="ngForm">
      <h2 style="margin-top: 0">Nouvel utilisateur</h2>
      <div class="form-grid">
        <label>Nom complet *<input name="nom" [(ngModel)]="nouveau.nomComplet" required /></label>
        <label>Login *<input name="login" [(ngModel)]="nouveau.login" required minlength="3" /></label>
        <label>Mot de passe * (6 car. min.)<input name="mdp" type="password" [(ngModel)]="nouveau.motDePasse" required minlength="6" /></label>
        <label>Rôle *
          <select name="role" [(ngModel)]="nouveau.role" required>
            @for (r of roles; track r) { <option [value]="r">{{ libelles[r] }}</option> }
          </select>
        </label>
      </div>
      @if (message()) { <p [class.error]="enErreur()" [class.success]="!enErreur()">{{ message() }}</p> }
      <div class="actions"><button class="btn" type="submit" [disabled]="f.invalid">Créer</button></div>
    </form>

    <h2>Comptes existants</h2>
    <table>
      <thead><tr><th>Nom</th><th>Login</th><th>Rôle</th><th>État</th><th></th></tr></thead>
      <tbody>
        @for (u of utilisateurs(); track u.id) {
          <tr>
            <td>{{ u.nomComplet }}</td>
            <td>{{ u.login }}</td>
            <td>
              <select [ngModel]="u.role" (ngModelChange)="modifier(u, { role: $event })">
                @for (r of roles; track r) { <option [value]="r">{{ libelles[r] }}</option> }
              </select>
            </td>
            <td><span class="badge" [class.ok]="u.actif" [class.ko]="!u.actif">{{ u.actif ? 'Actif' : 'Désactivé' }}</span></td>
            <td>
              <button class="btn small secondary" (click)="modifier(u, { actif: !u.actif })">{{ u.actif ? 'Désactiver' : 'Réactiver' }}</button>
              <button class="btn small secondary" (click)="reinitialiser(u)">Réinitialiser le mot de passe</button>
            </td>
          </tr>
        }
      </tbody>
    </table>
  `,
})
export class Utilisateurs implements OnInit {
  private readonly api = inject(UtilisateurApi);

  protected readonly roles = Object.keys(ROLE_LIBELLES) as Role[];
  protected readonly libelles = ROLE_LIBELLES;
  protected readonly utilisateurs = signal<Utilisateur[]>([]);
  protected readonly message = signal('');
  protected readonly enErreur = signal(false);

  protected nouveau = { nomComplet: '', login: '', motDePasse: '', role: 'CAISSIERE' as Role };

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.api.lister().subscribe((u) => this.utilisateurs.set(u));
  }

  creer(): void {
    this.api.creer(this.nouveau).subscribe({
      next: (u) => {
        this.afficher(`Compte ${u.login} créé`, false);
        this.nouveau = { nomComplet: '', login: '', motDePasse: '', role: 'CAISSIERE' };
        this.charger();
      },
      error: (e) => this.afficher(messageErreur(e), true),
    });
  }

  modifier(u: Utilisateur, changement: Partial<Pick<Utilisateur, 'role' | 'actif'>>): void {
    const maj = { nomComplet: u.nomComplet, role: u.role, actif: u.actif, ...changement };
    this.api.modifier(u.id, maj).subscribe({
      next: () => this.charger(),
      error: (e) => this.afficher(messageErreur(e), true),
    });
  }

  reinitialiser(u: Utilisateur): void {
    const mdp = prompt(`Nouveau mot de passe pour ${u.login} (6 caractères minimum) :`);
    if (!mdp) return;
    this.api.reinitialiserMotDePasse(u.id, mdp).subscribe({
      next: () => this.afficher(`Mot de passe de ${u.login} réinitialisé`, false),
      error: (e) => this.afficher(messageErreur(e), true),
    });
  }

  private afficher(texte: string, erreur: boolean): void {
    this.message.set(texte);
    this.enErreur.set(erreur);
  }
}
