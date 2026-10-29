import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ProduitApi } from '../../core/api';
import { AuthService } from '../../core/auth.service';
import { Produit } from '../../core/models';

import { messageErreur } from '../../core/auth.interceptor';

@Component({
  selector: 'app-produit-list',
  imports: [FormsModule, RouterLink, DecimalPipe],
  template: `
    <h1>Produits</h1>
    <div class="toolbar">
      <label>Rechercher
        <input [(ngModel)]="recherche" (keyup.enter)="charger()" placeholder="Code, désignation, code-barres" />
      </label>
      <label style="flex-direction: row; align-items: center">
        <input type="checkbox" [(ngModel)]="inclureInactifs" (change)="charger()" /> Inclure les archivés
      </label>
      <button class="btn secondary" (click)="charger()">Rechercher</button>
      @if (peutModifier) {
        <a class="btn" routerLink="/produits/nouveau">+ Nouveau produit</a>
      }
    </div>

        @if (erreur()) { <p class="error">{{ erreur() }}</p> }
    <table>
      <thead>
        <tr>
          <th>Code</th><th>Désignation</th><th>Catégorie</th>
          <th class="right">Prix vente</th><th class="right">Stock</th><th></th>
        </tr>
      </thead>
      <tbody>
        @for (p of produits(); track p.id) {
          <tr [class.muted]="!p.actif">
            <td>{{ p.code }}</td>
            <td>{{ p.designation }} @if (!p.actif) { <span class="badge">archivé</span> }</td>
            <td>{{ p.categorieNom }}</td>
            <td class="right">{{ p.prixVente | number: '1.0-2' }}</td>
            <td class="right">
              <span class="badge" [class.ok]="!p.enAlerte" [class.ko]="p.enAlerte">{{ p.stockActuel }}</span>
              <span class="muted"> {{ p.unite }}</span>
            </td>
            
            <td>
              @if (peutModifier) {
                <a class="btn small secondary" [routerLink]="['/produits', p.id]">Modifier</a>
                              <button class="btn small danger" (click)="supprimer(p)">Supprimer</button>
              }
            </td>
          </tr>
        } @empty {
          <tr><td colspan="6" class="muted">Aucun produit</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class ProduitList implements OnInit {
  private readonly api = inject(ProduitApi);
  protected readonly peutModifier = inject(AuthService).aRole('ADMIN', 'ACHATS');

  protected recherche = '';
  protected inclureInactifs = false;
  protected readonly produits = signal<Produit[]>([]);
  protected readonly erreur = signal('');

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.api.lister(this.recherche, !this.inclureInactifs).subscribe((p) => this.produits.set(p));
  }

    supprimer(p: Produit): void {
    if (!confirm(`Supprimer définitivement « ${p.designation} » ?`)) return;
    this.erreur.set('');
    this.api.supprimer(p.id).subscribe({
      next: () => this.charger(),
      error: (e) => this.erreur.set(messageErreur(e)),
    });
  }
}
