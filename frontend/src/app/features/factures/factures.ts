import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { VenteApi } from '../../core/api';
import { messageErreur } from '../../core/auth.interceptor';
import { AuthService } from '../../core/auth.service';
import { Facture } from '../../core/models';
import { FactureDetail } from './facture-detail';

@Component({
  selector: 'app-factures',
  imports: [FormsModule, DatePipe, DecimalPipe, FactureDetail],
  template: `
    @if (selection(); as f) {
      <div class="toolbar no-print">
        <button class="btn secondary" (click)="selection.set(null)">← Retour</button>
        <button class="btn" (click)="imprimer()">Imprimer</button>
        @if (peutAnnuler && f.statut === 'VALIDEE') {
          <button class="btn danger" (click)="annuler(f)">Annuler la facture</button>
        }
      </div>
      @if (erreur()) { <p class="error no-print">{{ erreur() }}</p> }
      <app-facture-detail [facture]="f" />
    } @else {
      <h1>Factures</h1>
      <div class="toolbar">
        <label>Du<input type="date" [(ngModel)]="du" (change)="charger()" /></label>
        <label>Au<input type="date" [(ngModel)]="au" (change)="charger()" /></label>
        <span class="muted">Total validé : <strong>{{ total() | number: '1.0-2' }} FCFA</strong></span>
      </div>
      <table>
        <thead>
          <tr><th>Numéro</th><th>Date</th><th>Client</th><th>Paiement</th><th class="right">Total</th><th>Statut</th><th>Caissier</th><th></th></tr>
        </thead>
        <tbody>
          @for (f of factures(); track f.id) {
            <tr>
              <td>{{ f.numero }}</td>
              <td>{{ f.date | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ f.client }}</td>
              <td>{{ f.modePaiement }}</td>
              <td class="right">{{ f.totalTtc | number: '1.0-2' }}</td>
              <td><span class="badge" [class.ok]="f.statut === 'VALIDEE'" [class.ko]="f.statut === 'ANNULEE'">{{ f.statut }}</span></td>
              <td>{{ f.caissier }}</td>
              <td><button class="btn small secondary" (click)="ouvrir(f.id)">Voir</button></td>
            </tr>
          } @empty {
            <tr><td colspan="8" class="muted">Aucune facture sur la période</td></tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class Factures implements OnInit {
  private readonly api = inject(VenteApi);
  protected readonly peutAnnuler = inject(AuthService).aRole('DIRECTION', 'ADMIN');

  protected du = new Date().toISOString().slice(0, 10);
  protected au = this.du;
  protected readonly factures = signal<Facture[]>([]);
  protected readonly total = signal(0);
  protected readonly selection = signal<Facture | null>(null);
  protected readonly erreur = signal('');

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.api.factures(this.du, this.au).subscribe((f) => {
      this.factures.set(f);
      this.total.set(f.filter((x) => x.statut === 'VALIDEE').reduce((s, x) => s + x.totalTtc, 0));
    });
  }

  ouvrir(id: number): void {
    this.erreur.set('');
    this.api.facture(id).subscribe((f) => this.selection.set(f));
  }

  annuler(f: Facture): void {
    const motif = prompt(`Motif d'annulation de la facture ${f.numero} :`);
    if (!motif?.trim()) return;
    this.api.annuler(f.id, motif.trim()).subscribe({
      next: (maj) => {
        this.selection.set(maj);
        this.charger();
      },
      error: (e) => this.erreur.set(messageErreur(e)),
    });
  }

  imprimer(): void {
    window.print();
  }
}
