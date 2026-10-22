import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';

import { Facture } from '../../core/models';

/** Affichage imprimable d'une facture (Ctrl+P / bouton Imprimer). TODO Sprint 2 : PDF généré par l'API. */
@Component({
  selector: 'app-facture-detail',
  imports: [DatePipe, DecimalPipe],
  template: `
    @let f = facture();
    <div class="card facture">
      <div style="display: flex; justify-content: space-between">
        <div>
          <h2 style="margin: 0">FACTURE {{ f.numero }}</h2>
          <div class="muted">{{ f.date | date: 'dd/MM/yyyy HH:mm' }} · Caissier : {{ f.caissier }}</div>
        </div>
        <div class="right">
          <strong>Client :</strong> {{ f.client }}<br />
          Paiement : {{ f.modePaiement }}
          @if (f.statut === 'ANNULEE') {
            <div><span class="badge ko">ANNULÉE</span> {{ f.motifAnnulation }}</div>
          }
        </div>
      </div>
      <table style="margin-top: 1rem">
        <thead>
                    <tr><th>Désignation</th><th class="right">Qté</th><th class="right">Prix unitaire</th><th class="right">Remise</th><th class="right">Total</th></tr>
        </thead>
        <tbody>
          @for (l of f.lignes; track $index) {
            <tr>
              <td>{{ l.designation }}</td>
              <td class="right">{{ l.quantite }}</td>
              <td class="right">{{ l.prixUnitaire | number: '1.0-2' }}</td>
              <td class="right">{{ l.remise | number: '1.0-2' }}</td>
              <!-- <td class="right">{{ l.tauxTva }} %</td> -->
              <td class="right">{{ l.totalLigne | number: '1.0-2' }}</td>
            </tr>
          }
        </tbody>
        <tfoot>
                    @if (f.totalTva > 0) {
            <!-- Seulement pour les anciennes factures émises avec TVA -->
            <tr><td colspan="4" class="right">Total HT</td><td class="right">{{ f.totalHt | number: '1.0-2' }}</td></tr>
            <tr><td colspan="4" class="right">TVA</td><td class="right">{{ f.totalTva | number: '1.0-2' }}</td></tr>
          }
          <tr><td colspan="4" class="right"><strong>Total</strong></td><td class="right"><strong>{{ f.totalTtc | number: '1.0-2' }} FCFA</strong></td></tr>
          <!-- <tr><td colspan="5" class="right">Total HT</td><td class="right">{{ f.totalHt | number: '1.0-2' }}</td></tr>
          <tr><td colspan="5" class="right">TVA</td><td class="right">{{ f.totalTva | number: '1.0-2' }}</td></tr>
          <tr><td colspan="5" class="right"><strong>Total TTC</strong></td><td class="right"><strong>{{ f.totalTtc | number: '1.0-2' }} FCFA</strong></td></tr> -->
        </tfoot>
      </table>
    </div>
  `,
})
export class FactureDetail {
  readonly facture = input.required<Facture>();
}
