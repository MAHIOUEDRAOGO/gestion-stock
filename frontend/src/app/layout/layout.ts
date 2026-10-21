import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { Role, ROLE_LIBELLES } from '../core/models';

interface EntreeMenu {
  libelle: string;
  lien: string;
  roles: Role[] | 'tous';
}

const MENU: EntreeMenu[] = [
  { libelle: 'Tableau de bord', lien: '/tableau-de-bord', roles: 'tous' },
  { libelle: 'Caisse', lien: '/caisse', roles: ['CAISSIERE', 'ADMIN'] },
  { libelle: 'Factures', lien: '/factures', roles: ['CAISSIERE', 'DIRECTION', 'ADMIN'] },
  { libelle: 'Produits', lien: '/produits', roles: 'tous' },
  { libelle: 'Mouvements de stock', lien: '/mouvements', roles: ['MAGASINIER', 'ACHATS', 'DIRECTION', 'ADMIN'] },
  { libelle: 'Alertes de stock', lien: '/alertes', roles: 'tous' },
  { libelle: "Journal d'activité", lien: '/audit', roles: ['DIRECTION', 'ADMIN'] },
  { libelle: 'Utilisateurs', lien: '/utilisateurs', roles: ['ADMIN'] },
];

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand">Gestion de stock</div>
        <nav>
          @for (e of menu(); track e.lien) {
            <a [routerLink]="e.lien" routerLinkActive="active">{{ e.libelle }}</a>
          }
        </nav>
      </aside>
      <div class="main">
        <header class="topbar">
          @if (auth.utilisateur(); as u) {
            <span>{{ u.nomComplet }} <span class="muted">· {{ libelleRole() }}</span></span>
          }
          <button class="btn secondary" (click)="auth.logout()">Déconnexion</button>
        </header>
        <main class="content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class Layout {
  protected readonly auth = inject(AuthService);

  protected readonly menu = computed(() => {
    const role = this.auth.utilisateur()?.role;
    return MENU.filter((e) => e.roles === 'tous' || (role && e.roles.includes(role)));
  });

  protected readonly libelleRole = computed(() => {
    const role = this.auth.utilisateur()?.role;
    return role ? ROLE_LIBELLES[role] : '';
  });
}
