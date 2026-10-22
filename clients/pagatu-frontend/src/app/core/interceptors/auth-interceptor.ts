import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth-service';
import { environment } from '../../../environments/environment';
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  if (!req.url.startsWith(environment.apiBaseUrl + '/')) return next(req);
  const token = auth.accessToken();
  return next(token ? req.clone({setHeaders:{Authorization:`Bearer ${token}`}}) : req).pipe(
    catchError((e: HttpErrorResponse) => {
      // Explicitly avoid silently redirecting on public endpoints.
      if (e.status === 401 && auth.autenticado()) void auth.iniciarLogin(location.pathname);
      return throwError(() => e);
    }),
  );
};
