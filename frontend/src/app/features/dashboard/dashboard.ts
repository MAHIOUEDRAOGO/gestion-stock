import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { StockApi, VenteApi } from '../../core/api';
import { AuthService } from '../../core/auth.service';
import { Produit } from '../../core/models';

/** Tableau de bord simple. TODO Sprint 4 : indicateurs détaillés pour la direction (graphiques). */
@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DecimalPipe],
  template: `
    <h1>Bonjour {{ auth.utilisateur()?.nomComplet }}</h1>

    <div class="grid">
      <div class="card">
        <div class="muted">Produits en alerte</div>
        <div class="stat" [class.error]="alertes().length > 0">{{ alertes().length }}</div>
        <a routerLink="/alertes">Voir le détail</a>
      </div>
      @if (voitVentes) {
        <div class="card">
          <div class="muted">Ventes du jour</div>
          <div class="stat">{{ caJour() | number: '1.0-0' }} FCFA</div>
          <span class="muted">{{ nbFactures() }} facture(s)</span>
        </div>
      }
    </div>

    @if (alertes().length > 0) {
      <h2>Produits à réapprovisionner</h2>
      <table>
        <thead><tr><th>Code</th><th>Désignation</th><th class="right">Stock</th><th class="right">Seuil</th></tr></thead>
        <tbody>
          @for (p of alertes().slice(0, 10); track p.id) {
            <tr>
              <td>{{ p.code }}</td>
              <td>{{ p.designation }}</td>
              <td class="right"><span class="badge" [class.ko]="p.stockActuel === 0" [class.warn]="p.stockActuel > 0">{{ p.stockActuel }}</span></td>
              <td class="right">{{ p.seuilAlerte }}</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class Dashboard implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly stockApi = inject(StockApi);
  private readonly venteApi = inject(VenteApi);

  protected readonly alertes = signal<Produit[]>([]);
  protected readonly caJour = signal(0);
  protected readonly nbFactures = signal(0);
  protected readonly voitVentes = this.auth.aRole('CAISSIERE', 'DIRECTION', 'ADMIN');

  ngOnInit(): void {
    this.stockApi.alertes().subscribe((a) => this.alertes.set(a));
    if (this.voitVentes) {
      this.venteApi.factures().subscribe((f) => {
        const valides = f.filter((x) => x.statut === 'VALIDEE');
        this.nbFactures.set(valides.length);
        this.caJour.set(valides.reduce((s, x) => s + x.totalTtc, 0));
      });
    }
  }
}
