import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth-service';
@Component({selector:'app-auth-callback',template:`
  <main class="auth-screen"><div class="auth-card"><div class="brand-mark">P</div>
  <h2>Inicio de sesión</h2>
  @if (error()) { <p class="alert error">{{error()}}</p><a href="/">Volver al inicio</a> }
  @else { <p>Comprobando la respuesta de Keycloak y el código PKCE…</p><div class="spinner"></div> }
  </div></main>`})
export class AuthCallback {
  protected readonly error = signal<string | null>(null);
  constructor() {
    const router = inject(Router), route = inject(ActivatedRoute), auth = inject(AuthService);
    const code = route.snapshot.queryParamMap.get('code');
    const state = route.snapshot.queryParamMap.get('state');
    const error = route.snapshot.queryParamMap.get('error');
    if (error) { this.error.set('Keycloak rechazó el inicio de sesión: ' + error); return; }
    if (!code || !state) { this.error.set('Falta el código de retorno de Keycloak.'); return; }
    void auth.completarLogin(code, state)
      .then(() => router.navigateByUrl(auth.destinoPendiente(), {replaceUrl:true}))
      .catch((err: unknown) => this.error.set(err instanceof Error ? err.message : 'No se pudo iniciar sesión.'));
  }
}
