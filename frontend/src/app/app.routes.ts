import { Routes } from '@angular/router';

import { authGuard, roleGuard } from './core/auth.guard';
import { Layout } from './layout/layout';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/login/login').then((m) => m.Login) },
  {
    path: '',
    component: Layout,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tableau-de-bord' },
      {
        path: 'tableau-de-bord',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'produits',
        loadComponent: () => import('./features/produits/produit-list').then((m) => m.ProduitList),
      },
      {
        path: 'produits/nouveau',
        canActivate: [roleGuard('ADMIN', 'ACHATS')],
        loadComponent: () => import('./features/produits/produit-form').then((m) => m.ProduitForm),
      },
      {
        path: 'produits/:id',
        canActivate: [roleGuard('ADMIN', 'ACHATS')],
        loadComponent: () => import('./features/produits/produit-form').then((m) => m.ProduitForm),
      },
      {
        path: 'mouvements',
        canActivate: [roleGuard('MAGASINIER', 'ACHATS', 'DIRECTION', 'ADMIN')],
        loadComponent: () => import('./features/stock/mouvements').then((m) => m.Mouvements),
      },
      {
        path: 'alertes',
        loadComponent: () => import('./features/stock/alertes').then((m) => m.Alertes),
      },
      {
        path: 'caisse',
        canActivate: [roleGuard('CAISSIERE', 'ADMIN')],
        loadComponent: () => import('./features/caisse/caisse').then((m) => m.Caisse),
      },
      {
        path: 'factures',
        canActivate: [roleGuard('CAISSIERE', 'DIRECTION', 'ADMIN')],
        loadComponent: () => import('./features/factures/factures').then((m) => m.Factures),
      },
      {
        path: 'audit',
        canActivate: [roleGuard('DIRECTION', 'ADMIN')],
        loadComponent: () => import('./features/audit/audit').then((m) => m.Audit),
      },
      {
        path: 'utilisateurs',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () => import('./features/utilisateurs/utilisateurs').then((m) => m.Utilisateurs),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
