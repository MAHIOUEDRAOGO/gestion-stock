import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import {
  Categorie, Client, Facture, FactureRequest, JournalAudit, Motif, Mouvement, MouvementRequest,
  Page, Produit, ProduitRequest, Role, Utilisateur,
} from './models';

/** Construit des paramètres HTTP en ignorant les valeurs vides. */
function params(valeurs: Record<string, string | number | boolean | null | undefined>): HttpParams {
  let p = new HttpParams();
  for (const [cle, valeur] of Object.entries(valeurs)) {
    if (valeur !== null && valeur !== undefined && valeur !== '') {
      p = p.set(cle, String(valeur));
    }
  }
  return p;
}

@Injectable({ providedIn: 'root' })
export class ProduitApi {
  private readonly http = inject(HttpClient);

  lister(recherche = '', actifsSeulement = true) {
    return this.http.get<Produit[]>('/api/produits', { params: params({ recherche, actifsSeulement }) });
  }
  obtenir(id: number) {
    return this.http.get<Produit>(`/api/produits/${id}`);
  }
  creer(p: ProduitRequest) {
    return this.http.post<Produit>('/api/produits', p);
  }
  modifier(id: number, p: ProduitRequest) {
    return this.http.put<Produit>(`/api/produits/${id}`, p);
  }

    supprimer(id: number) {
    return this.http.delete<void>(`/api/produits/${id}`);
  }

  categories() {
    return this.http.get<Categorie[]>('/api/categories');
  }
  creerCategorie(nom: string) {
    return this.http.post<Categorie>('/api/categories', { nom });
  }
}

@Injectable({ providedIn: 'root' })
export class StockApi {
  private readonly http = inject(HttpClient);

  mouvements(filtre: { produitId?: number | null; type?: string; motif?: string; du?: string; au?: string; page?: number }) {
    return this.http.get<Page<Mouvement>>('/api/stock/mouvements', { params: params(filtre) });
  }
  enregistrer(m: MouvementRequest) {
    return this.http.post<Mouvement>('/api/stock/mouvements', m);
  }
  alertes() {
    return this.http.get<Produit[]>('/api/stock/alertes');
  }
  motifs() {
    return this.http.get<Motif[]>('/api/stock/motifs');
  }
}

@Injectable({ providedIn: 'root' })
export class VenteApi {
  private readonly http = inject(HttpClient);

  creerFacture(f: FactureRequest) {
    return this.http.post<Facture>('/api/factures', f);
  }
  factures(du?: string, au?: string) {
    return this.http.get<Facture[]>('/api/factures', { params: params({ du, au }) });
  }
  facture(id: number) {
    return this.http.get<Facture>(`/api/factures/${id}`);
  }
  annuler(id: number, motif: string) {
    return this.http.post<Facture>(`/api/factures/${id}/annuler`, { motif });
  }
  clients(nom = '') {
    return this.http.get<Client[]>('/api/clients', { params: params({ nom }) });
  }
  creerClient(c: Omit<Client, 'id'>) {
    return this.http.post<Client>('/api/clients', c);
  }
}

@Injectable({ providedIn: 'root' })
export class AuditApi {
  private readonly http = inject(HttpClient);

  rechercher(filtre: { login?: string; action?: string; du?: string; au?: string; page?: number }) {
    return this.http.get<Page<JournalAudit>>('/api/audit', { params: params(filtre) });
  }
}

@Injectable({ providedIn: 'root' })
export class UtilisateurApi {
  private readonly http = inject(HttpClient);

  lister() {
    return this.http.get<Utilisateur[]>('/api/utilisateurs');
  }
  creer(u: { nomComplet: string; login: string; motDePasse: string; role: Role }) {
    return this.http.post<Utilisateur>('/api/utilisateurs', u);
  }
  modifier(id: number, u: { nomComplet: string; role: Role; actif: boolean }) {
    return this.http.put<Utilisateur>(`/api/utilisateurs/${id}`, u);
  }
  reinitialiserMotDePasse(id: number, motDePasse: string) {
    return this.http.post<void>(`/api/utilisateurs/${id}/reinitialiser-mot-de-passe`, motDePasse, {
      headers: { 'Content-Type': 'text/plain' },
    });
  }
}
