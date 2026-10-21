import { Component, inject, OnInit, signal } from '@angular/core';

import { StockApi } from '../../core/api';
import { Produit } from '../../core/models';

@Component({
  selector: 'app-alertes',
  template: `
    <h1>Alertes de stock</h1>
    <p class="muted">Produits dont le stock est inférieur ou égal au seuil d'alerte.</p>
    <table>
      <thead>
        <tr><th>Code</th><th>Désignation</th><th>Catégorie</th><th class="right">Stock</th><th class="right">Seuil</th><th>État</th></tr>
      </thead>
      <tbody>
        @for (p of produits(); track p.id) {
          <tr>
            <td>{{ p.code }}</td>
            <td>{{ p.designation }}</td>
            <td>{{ p.categorieNom }}</td>
            <td class="right">{{ p.stockActuel }} {{ p.unite }}</td>
            <td class="right">{{ p.seuilAlerte }}</td>
            <td>
              @if (p.stockActuel === 0) { <span class="badge ko">Rupture</span> }
              @else { <span class="badge warn">Stock bas</span> }
            </td>
          </tr>
        } @empty {
          <tr><td colspan="6" class="success">Aucune alerte : tous les stocks sont au-dessus du seuil.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class Alertes implements OnInit {
  private readonly api = inject(StockApi);
  protected readonly produits = signal<Produit[]>([]);

  ngOnInit(): void {
    this.api.alertes().subscribe((p) => this.produits.set(p));
  }
}
