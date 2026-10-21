import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';
import { Role } from './models';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.connecte() ? true : inject(Router).createUrlTree(['/login']);
};

/** Protège une page selon le rôle (la vraie sécurité reste côté API). */
export function roleGuard(...roles: Role[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    return auth.aRole(...roles) ? true : inject(Router).createUrlTree(['/tableau-de-bord']);
  };
}
