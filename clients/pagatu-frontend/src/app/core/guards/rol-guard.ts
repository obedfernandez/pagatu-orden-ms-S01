import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth-service';
export function rolGuard(...roles: string[]): CanActivateFn {
  return () => {
    if (inject(AuthService).tieneRol(...roles)) return true;
    alert('Tu cuenta no tiene permisos para esta función.');
    return inject(Router).createUrlTree(['/']);
  };
}
