export type Role = 'ADMIN' | 'DIRECTION' | 'MAGASINIER' | 'CAISSIERE' | 'ACHATS';

export const ROLE_LIBELLES: Record<Role, string> = {
  ADMIN: 'Administrateur',
  DIRECTION: 'Direction',
  MAGASINIER: 'Magasinier',
  CAISSIERE: 'Caissière',
  ACHATS: 'Responsable achats',
};

export interface Utilisateur {
  id: number;
  nomComplet: string;
  login: string;
  role: Role;
  actif: boolean;
}

export interface LoginResponse {
  token: string;
  utilisateur: Utilisateur;
}

export interface Categorie {
  id: number;
  nom: string;
}

export interface Produit {
  id: number;
  code: string;
  codeBarre: string | null;
  designation: string;
  categorieId: number | null;
  categorieNom: string | null;
  unite: string;
  prixAchat: number;
  prixVente: number;
  tauxTva: number;
  seuilAlerte: number;
  stockActuel: number;
  emplacement: string | null;
  actif: boolean;
  enAlerte: boolean;
}

export type ProduitRequest = Omit<Produit, 'id' | 'categorieNom' | 'stockActuel' | 'enAlerte' | 'tauxTva'> & {
  /** Facultatif : sans valeur, l'API applique le taux par défaut. */
  tauxTva?: number;
  /** Quantité déjà en magasin, uniquement à la création. */
  stockInitial?: number;
};

export type TypeMouvement = 'ENTREE' | 'SORTIE';

export interface Motif {
  code: string;
  type: TypeMouvement;
}

export interface Mouvement {
  id: number;
  date: string;
  produitId: number;
  produitCode: string;
  produitDesignation: string;
  type: TypeMouvement;
  motif: string;
  quantite: number;
  stockApres: number;
  reference: string | null;
  commentaire: string | null;
  utilisateur: string;
}

export interface MouvementRequest {
  produitId: number;
  motif: string;
  quantite: number;
  reference?: string;
  commentaire?: string;
}

export type ModePaiement = 'ESPECES' | 'MOBILE_MONEY' | 'CARTE' | 'CHEQUE' | 'CREDIT';

export interface Client {
  id: number;
  nom: string;
  telephone: string | null;
  adresse: string | null;
}

export interface LigneFacture {
  produitId: number;
  produitCode: string;
  designation: string;
  quantite: number;
  prixUnitaire: number;
  tauxTva: number;
  remise: number;
  totalLigne: number;
}

export interface Facture {
  id: number;
  numero: string;
  date: string;
  clientId: number | null;
  client: string;
  totalHt: number;
  totalTva: number;
  totalTtc: number;
  modePaiement: ModePaiement;
  statut: 'VALIDEE' | 'ANNULEE';
  motifAnnulation: string | null;
  caissier: string;
  lignes: LigneFacture[];
}

export interface FactureRequest {
  clientId: number | null;
  modePaiement: ModePaiement;
  lignes: { produitId: number; quantite: number; remise: number }[];
}

export interface JournalAudit {
  id: number;
  dateAction: string;
  utilisateurLogin: string | null;
  action: string;
  entite: string | null;
  entiteId: number | null;
  details: string | null;
  adresseIp: string | null;
}

/** Page renvoyée par Spring Data. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}
