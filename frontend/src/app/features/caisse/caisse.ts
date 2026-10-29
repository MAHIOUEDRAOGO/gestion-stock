import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ProduitApi, VenteApi } from '../../core/api';
import { messageErreur } from '../../core/auth.interceptor';
import { Client, Facture, ModePaiement, Produit } from '../../core/models';
import { FactureDetail } from '../factures/facture-detail';

interface LignePanier {
  produit: Produit;
  quantite: number;
  remise: number;
}

@Component({
  selector: 'app-caisse',
  imports: [FormsModule, DecimalPipe, FactureDetail],
  template: `
    @if (derniereFacture(); as f) {
      <div class="toolbar no-print">
        <button class="btn" (click)="imprimer()">Imprimer</button>
        <button class="btn secondary" (click)="nouvelleVente()">Nouvelle vente</button>
      </div>
      <app-facture-detail [facture]="f" />
    } @else {
      <h1>Caisse</h1>
      <div class="toolbar">
        <label style="flex: 1">Produit (code, code-barres ou nom puis Entrée)
          <input [(ngModel)]="recherche" (keyup.enter)="chercher()" autofocus />
        </label>
        <button class="btn secondary" (click)="chercher()">Chercher</button>
      </div>

      @if (resultats().length > 1) {
        <div class="card" style="margin-bottom: 1rem">
          @for (p of resultats(); track p.id) {
            <button class="btn small secondary" style="margin: .2rem" (click)="ajouter(p)">
              {{ p.designation }} ({{ p.stockActuel }})
            </button>
          }
        </div>
      }

      <table>
        <thead>
          <tr><th>Produit</th><th class="right">Prix unitaire</th><th>Quantité</th><th>Remise</th><th class="right">Total</th><th></th></tr>
        </thead>
        <tbody>
          @for (l of panier(); track l.produit.id) {
            <tr>
              <td>{{ l.produit.designation }} <span class="muted">(stock : {{ l.produit.stockActuel }})</span></td>
              <td class="right">{{ l.produit.prixVente | number: '1.0-2' }}</td>
              <td><input type="number" min="1" [max]="l.produit.stockActuel" style="width: 80px" [(ngModel)]="l.quantite" (ngModelChange)="rafraichir()" /></td>
              <td><input type="number" min="0" style="width: 100px" [(ngModel)]="l.remise" (ngModelChange)="rafraichir()" /></td>
              <td class="right">{{ totalLigne(l) | number: '1.0-2' }}</td>
              <td><button class="btn small danger" (click)="retirer(l)">✕</button></td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="muted">Panier vide : scannez ou recherchez un produit.</td></tr>
          }
        </tbody>
      </table>

      <div class="card" style="margin-top: 1rem">
        <div class="form-grid">
          <label>Client
            <select [(ngModel)]="clientId">
              <option [ngValue]="null">Client de passage</option>
              @for (c of clients(); track c.id) { <option [ngValue]="c.id">{{ c.nom }}</option> }
            </select>
          </label>
          <label>Mode de paiement
            <select [(ngModel)]="modePaiement">
              <option value="ESPECES">Espèces</option>
              <option value="MOBILE_MONEY">Mobile money</option>
              <option value="CARTE">Carte</option>
              <option value="CHEQUE">Chèque</option>
              <option value="CREDIT">Crédit client</option>
            </select>
          </label>
          <div class="right">
            
            <div class="stat">{{ totaux().ttc | number: '1.0-2' }} FCFA</div>
          </div>
        </div>
        @if (erreur()) { <p class="error">{{ erreur() }}</p> }
        <div class="actions">
          <button class="btn" [disabled]="panier().length === 0 || envoi()" (click)="valider()">Valider la vente</button>
          <button class="btn secondary" (click)="nouvelleVente()">Vider</button>
        </div>
      </div>
    }
  `,
})
export class Caisse implements OnInit {
  private readonly produitApi = inject(ProduitApi);
  private readonly venteApi = inject(VenteApi);

  protected recherche = '';
  protected clientId: number | null = null;
  protected modePaiement: ModePaiement = 'ESPECES';

  protected readonly resultats = signal<Produit[]>([]);
  protected readonly panier = signal<LignePanier[]>([]);
  protected readonly clients = signal<Client[]>([]);
  protected readonly erreur = signal('');
  protected readonly envoi = signal(false);
  protected readonly derniereFacture = signal<Facture | null>(null);

  /** Totaux indicatifs : le calcul qui fait foi est celui du serveur. */
  protected readonly totaux = computed(() => {
    let ht = 0;
    let tva = 0;
    for (const l of this.panier()) {
      const ligne = this.totalLigne(l);
      ht += ligne;
      tva += (ligne * l.produit.tauxTva) / 100;
    }
    return { ht, tva, ttc: ht + tva };
  });

  ngOnInit(): void {
    this.venteApi.clients().subscribe((c) => this.clients.set(c));
  }

  chercher(): void {
    const terme = this.recherche.trim();
    if (!terme) return;
    this.produitApi.lister(terme).subscribe((produits) => {
      // Un code-barres scanné correspond exactement à un produit : ajout direct
      const exact = produits.find((p) => p.codeBarre === terme || p.code === terme);
      if (exact || produits.length === 1) {
        this.ajouter(exact ?? produits[0]);
      } else {
        this.resultats.set(produits);
        this.erreur.set(produits.length === 0 ? 'Aucun produit trouvé' : '');
      }
    });
  }

  ajouter(p: Produit): void {
    this.erreur.set('');
    this.resultats.set([]);
    this.recherche = '';
    if (p.stockActuel <= 0) {
      this.erreur.set(`${p.designation} est en rupture de stock`);
      return;
    }
    const existante = this.panier().find((l) => l.produit.id === p.id);
    if (existante) {
      existante.quantite++;
      this.rafraichir();
    } else {
      this.panier.update((lignes) => [...lignes, { produit: p, quantite: 1, remise: 0 }]);
    }
  }

  retirer(l: LignePanier): void {
    this.panier.update((lignes) => lignes.filter((x) => x !== l));
  }

  rafraichir(): void {
    this.panier.update((lignes) => [...lignes]);
  }

  totalLigne(l: LignePanier): number {
    return l.produit.prixVente * (l.quantite || 0) - (l.remise || 0);
  }

  valider(): void {
    this.envoi.set(true);
    this.erreur.set('');
    this.venteApi
      .creerFacture({
        clientId: this.clientId,
        modePaiement: this.modePaiement,
        lignes: this.panier().map((l) => ({ produitId: l.produit.id, quantite: l.quantite, remise: l.remise || 0 })),
      })
      .subscribe({
        next: (f) => {
          this.derniereFacture.set(f);
          this.envoi.set(false);
        },
        error: (e) => {
          this.erreur.set(messageErreur(e));
          this.envoi.set(false);
        },
      });
  }

  imprimer(): void {
    window.print();
  }

  nouvelleVente(): void {
    this.panier.set([]);
    this.derniereFacture.set(null);
    this.clientId = null;
    this.modePaiement = 'ESPECES';
    this.erreur.set('');
  }
}
