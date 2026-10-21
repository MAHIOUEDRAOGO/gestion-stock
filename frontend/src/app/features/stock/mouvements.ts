import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ProduitApi, StockApi } from '../../core/api';
import { messageErreur } from '../../core/auth.interceptor';
import { AuthService } from '../../core/auth.service';
import { Motif, Mouvement, Produit } from '../../core/models';

@Component({
  selector: 'app-mouvements',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>Mouvements de stock</h1>

    @if (peutSaisir) {
      <form class="card" (ngSubmit)="enregistrer()" #f="ngForm">
        <h2 style="margin-top: 0">Nouvelle entrée / sortie</h2>
        <div class="form-grid">
          <label>Produit *
            <select name="produitId" [(ngModel)]="saisie.produitId" required>
              @for (p of produits(); track p.id) {
                <option [ngValue]="p.id">{{ p.code }} – {{ p.designation }} (stock : {{ p.stockActuel }})</option>
              }
            </select>
          </label>
          <label>Motif *
            <select name="motif" [(ngModel)]="saisie.motif" required>
              <optgroup label="Entrées">
                @for (m of motifsEntree(); track m.code) { <option [value]="m.code">{{ libelle(m.code) }}</option> }
              </optgroup>
              <optgroup label="Sorties">
                @for (m of motifsSortie(); track m.code) { <option [value]="m.code">{{ libelle(m.code) }}</option> }
              </optgroup>
            </select>
          </label>
          <label>Quantité *<input name="quantite" type="number" min="1" [(ngModel)]="saisie.quantite" required /></label>
          <label>Référence (bon, BL…)<input name="reference" [(ngModel)]="saisie.reference" /></label>
          <label>Commentaire<input name="commentaire" [(ngModel)]="saisie.commentaire" /></label>
        </div>
        @if (message()) { <p [class.error]="messageErreur()" [class.success]="!messageErreur()">{{ message() }}</p> }
        <div class="actions"><button class="btn" type="submit" [disabled]="f.invalid">Enregistrer</button></div>
      </form>
    }

    <h2>Historique</h2>
    <div class="toolbar">
      <label>Type
        <select [(ngModel)]="filtre.type" (change)="charger()">
          <option value="">Tous</option><option value="ENTREE">Entrées</option><option value="SORTIE">Sorties</option>
        </select>
      </label>
      <label>Du<input type="date" [(ngModel)]="filtre.du" (change)="charger()" /></label>
      <label>Au<input type="date" [(ngModel)]="filtre.au" (change)="charger()" /></label>
    </div>
    <table>
      <thead>
        <tr><th>Date</th><th>Produit</th><th>Type</th><th>Motif</th><th class="right">Qté</th><th class="right">Stock après</th><th>Référence</th><th>Par</th></tr>
      </thead>
      <tbody>
        @for (m of mouvements(); track m.id) {
          <tr>
            <td>{{ m.date | date: 'dd/MM/yyyy HH:mm' }}</td>
            <td>{{ m.produitCode }} – {{ m.produitDesignation }}</td>
            <td><span class="badge" [class.entree]="m.type === 'ENTREE'" [class.sortie]="m.type === 'SORTIE'">{{ m.type === 'ENTREE' ? 'Entrée' : 'Sortie' }}</span></td>
            <td>{{ libelle(m.motif) }}</td>
            <td class="right">{{ m.quantite }}</td>
            <td class="right">{{ m.stockApres }}</td>
            <td>{{ m.reference }}</td>
            <td>{{ m.utilisateur }}</td>
          </tr>
        } @empty {
          <tr><td colspan="8" class="muted">Aucun mouvement sur la période</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class Mouvements implements OnInit {
  private readonly stockApi = inject(StockApi);
  private readonly produitApi = inject(ProduitApi);
  protected readonly peutSaisir = inject(AuthService).aRole('MAGASINIER', 'ADMIN');

  protected readonly produits = signal<Produit[]>([]);
  protected readonly motifs = signal<Motif[]>([]);
  protected readonly mouvements = signal<Mouvement[]>([]);
  protected readonly message = signal('');
  protected readonly messageErreur = signal(false);
  protected readonly motifsEntree = computed(() => this.motifs().filter((m) => m.type === 'ENTREE'));
  protected readonly motifsSortie = computed(() => this.motifs().filter((m) => m.type === 'SORTIE'));

  protected saisie = { produitId: null as number | null, motif: '', quantite: 1, reference: '', commentaire: '' };
  protected filtre = { type: '', du: '', au: '' };

  ngOnInit(): void {
    if (this.peutSaisir) {
      this.produitApi.lister().subscribe((p) => this.produits.set(p));
      this.stockApi.motifs().subscribe((m) => this.motifs.set(m));
    }
    this.charger();
  }

  charger(): void {
    this.stockApi.mouvements(this.filtre).subscribe((page) => this.mouvements.set(page.content));
  }

  enregistrer(): void {
    const { produitId, ...reste } = this.saisie;
    if (produitId === null) return;
    this.stockApi.enregistrer({ produitId, ...reste }).subscribe({
      next: (m) => {
        this.messageErreur.set(false);
        this.message.set(`Mouvement enregistré. Nouveau stock de ${m.produitDesignation} : ${m.stockApres}`);
        this.saisie = { produitId: null, motif: '', quantite: 1, reference: '', commentaire: '' };
        this.produitApi.lister().subscribe((p) => this.produits.set(p));
        this.charger();
      },
      error: (e) => {
        this.messageErreur.set(true);
        this.message.set(messageErreur(e));
      },
    });
  }

  libelle(code: string): string {
    const texte = code.replaceAll('_', ' ').toLowerCase();
    return texte.charAt(0).toUpperCase() + texte.slice(1);
  }
}
