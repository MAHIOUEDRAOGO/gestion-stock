import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuditApi } from '../../core/api';
import { JournalAudit } from '../../core/models';

@Component({
  selector: 'app-audit',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>Journal d'activité</h1>
    <p class="muted">Toutes les actions des utilisateurs (connexions, ventes, mouvements, modifications…).</p>
    <div class="toolbar">
      <label>Utilisateur (login)<input [(ngModel)]="filtre.login" (keyup.enter)="rechercher()" /></label>
      <label>Action<input [(ngModel)]="filtre.action" (keyup.enter)="rechercher()" placeholder="ex. CREATION_FACTURE" /></label>
      <label>Du<input type="date" [(ngModel)]="filtre.du" /></label>
      <label>Au<input type="date" [(ngModel)]="filtre.au" /></label>
      <button class="btn" (click)="rechercher()">Filtrer</button>
    </div>
    <table>
      <thead><tr><th>Date</th><th>Utilisateur</th><th>Action</th><th>Détails</th><th>Poste (IP)</th></tr></thead>
      <tbody>
        @for (j of lignes(); track j.id) {
          <tr>
            <td>{{ j.dateAction | date: 'dd/MM/yyyy HH:mm:ss' }}</td>
            <td>{{ j.utilisateurLogin }}</td>
            <td><span class="badge" [class.ko]="j.action.includes('ECHOUEE') || j.action.includes('ANNULATION')">{{ j.action }}</span></td>
            <td>{{ j.details }}</td>
            <td class="muted">{{ j.adresseIp }}</td>
          </tr>
        } @empty {
          <tr><td colspan="5" class="muted">Aucune activité</td></tr>
        }
      </tbody>
    </table>
    <div class="actions">
      <button class="btn secondary small" [disabled]="page() === 0" (click)="allerA(page() - 1)">← Précédent</button>
      <span class="muted">Page {{ page() + 1 }} / {{ totalPages() || 1 }}</span>
      <button class="btn secondary small" [disabled]="page() + 1 >= totalPages()" (click)="allerA(page() + 1)">Suivant →</button>
    </div>
  `,
})
export class Audit implements OnInit {
  private readonly api = inject(AuditApi);

  protected filtre = { login: '', action: '', du: '', au: '' };
  protected readonly lignes = signal<JournalAudit[]>([]);
  protected readonly page = signal(0);
  protected readonly totalPages = signal(0);

  ngOnInit(): void {
    this.rechercher();
  }

  rechercher(): void {
    this.allerA(0);
  }

  allerA(page: number): void {
    this.api.rechercher({ ...this.filtre, page }).subscribe((p) => {
      this.lignes.set(p.content);
      this.page.set(p.number);
      this.totalPages.set(p.totalPages);
    });
  }
}
