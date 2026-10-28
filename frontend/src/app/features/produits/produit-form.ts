import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ProduitApi } from '../../core/api';
import { messageErreur } from '../../core/auth.interceptor';
import { Categorie, ProduitRequest } from '../../core/models';

@Component({
  selector: 'app-produit-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <h1>{{ id() ? 'Modifier le produit' : 'Nouveau produit' }}</h1>
    <form class="card" [formGroup]="form" (ngSubmit)="enregistrer()">
      <div class="form-grid">
        <!-- <label>Code *<input formControlName="code" /></label>
        <label>Code-barres<input formControlName="codeBarre" /></label> -->
        <label>Désignation *<input formControlName="designation" /></label>
        <label>Catégorie
          <select formControlName="categorieId">
            <option [ngValue]="null">—</option>
            @for (c of categories(); track c.id) {
              <option [ngValue]="c.id">{{ c.nom }}</option>
            }
          </select>
        </label>
        <label>Unité *<input formControlName="unite" placeholder="pièce, kg, sac…" /></label>
                @if (!id()) {
          <label>Quantité en stock *
            <input type="number" min="0" formControlName="stockInitial" />
            <span class="hint">Nombre d'unités déjà présentes en magasin</span>
          </label>
        }
        <label>Prix d'achat HT *<input type="number" min="0" formControlName="prixAchat" placeholder="Facultatif"/></label>
        <label>Prix de vente  *<input type="number" min="0" formControlName="prixVente" /></label>
       <!-- <label>TVA (%) *<input type="number" min="0" formControlName="tauxTva" /></label> -->
        <label>Seuil d'alerte *<input type="number" min="0" formControlName="seuilAlerte" /></label>
        <!-- <label>Emplacement<input formControlName="emplacement" placeholder="Rayon A3…" /></label> -->
        <label style="flex-direction: row; align-items: center; gap: .5rem">
          <input type="checkbox" formControlName="actif" /> Produit actif (décocher pour archiver)
        </label>
      </div>
      <p class="muted">Le stock ne se modifie pas ici : utilisez un mouvement de stock (entrée / sortie).</p>
      @if (erreur()) { <p class="error">{{ erreur() }}</p> }
      <div class="actions">
        <button class="btn" type="submit" [disabled]="form.invalid">Enregistrer</button>
        <a class="btn secondary" routerLink="/produits">Annuler</a>
      </div>
    </form>
  `,
})
export class ProduitForm implements OnInit {
  /** Paramètre de route :id (absent pour une création). */
  readonly id = input<string>();

  private readonly api = inject(ProduitApi);
  private readonly router = inject(Router);

  protected readonly categories = signal<Categorie[]>([]);
  protected readonly erreur = signal('');

  protected readonly form = inject(FormBuilder).nonNullable.group({
   // code: ['', Validators.required],
   // codeBarre: [''],
    designation: ['', Validators.required],
    categorieId: [null as number | null],
    unite: ['pièce', Validators.required],
    stockInitial: [0, [Validators.required, Validators.min(0)]],
   // prixAchat: [0, [Validators.required, Validators.min(0)]],
    prixAchat: [null as number | null, Validators.min(0)],
    prixVente: [0, [Validators.required, Validators.min(0)]],
    // tauxTva: [18, [Validators.required, Validators.min(0)]],
    seuilAlerte: [0, [Validators.required, Validators.min(0)]],
   // emplacement: [''],
    actif: [true],
  });

  ngOnInit(): void {
    this.api.categories().subscribe((c) => this.categories.set(c));
    const id = this.id();
    if (id) {
      this.api.obtenir(+id).subscribe((p) => this.form.patchValue(p))
       // this.form.patchValue({ ...p, codeBarre: p.codeBarre ?? '', emplacement: p.emplacement ?? '' }));
    }
  }

  enregistrer(): void {
    const valeur = this.form.getRawValue() as ProduitRequest;
    const id = this.id();
    const requete = id ? this.api.modifier(+id, valeur) : this.api.creer(valeur);
    requete.subscribe({
      next: () => this.router.navigate(['/produits']),
      error: (e) => this.erreur.set(messageErreur(e)),
    });
  }
}
