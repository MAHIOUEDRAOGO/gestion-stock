import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

/** Ajoute le jeton JWT à chaque appel /api et déconnecte si le jeton a expiré. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const requete = token && req.url.startsWith('/api')
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(requete).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.endsWith('/auth/login')) {
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};

/** Extrait le message d'erreur renvoyé par l'API (GlobalExceptionHandler). */
export function messageErreur(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    return err.error?.message ?? (err.status === 0 ? 'Serveur injoignable' : `Erreur ${err.status}`);
  }
  return 'Erreur inattendue';
}
