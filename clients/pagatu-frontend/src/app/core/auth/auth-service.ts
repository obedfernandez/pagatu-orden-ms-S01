import { Injectable, computed, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { codeChallenge, codeVerifier, stateAleatorio } from './pkce';
import { decodeJwtPayload } from './jwt';
interface Session { accessToken: string; idToken: string; refreshToken: string; expiresAt: number; }
const PREFIX = 'pagatu_s11_';
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sesion = signal<Session | null>(null);
  readonly autenticado = computed(() => !!this.sesion() && this.sesion()!.expiresAt > Date.now());
  readonly roles = computed(() => decodeJwtPayload(this.sesion()?.accessToken ?? '')?.realm_access?.roles ?? []);
  readonly idCliente = computed(() => decodeJwtPayload(this.sesion()?.accessToken ?? '')?.sub ?? null);
  readonly nombreUsuario = computed(() => decodeJwtPayload(this.sesion()?.accessToken ?? '')?.preferred_username ?? null);
  tieneRol(...roles: string[]) { return roles.some(r => this.roles().includes(r)); }
  accessToken() { return this.autenticado() ? this.sesion()?.accessToken ?? null : null; }

  /** Tokens only live in JS memory; verifier and state survive just one redirect. */
  async iniciarLogin(destino = location.pathname + location.search): Promise<void> {
    const verifier = codeVerifier(); const state = stateAleatorio();
    sessionStorage.setItem(PREFIX + 'verifier', verifier);
    sessionStorage.setItem(PREFIX + 'state', state);
    sessionStorage.setItem(PREFIX + 'destino', destino.startsWith('/') && !destino.startsWith('//') ? destino : '/');
    const search = new URLSearchParams({client_id: environment.keycloak.clientId, response_type: 'code',
      redirect_uri: environment.keycloak.redirectUri, scope:'openid profile', state,
      code_challenge: await codeChallenge(verifier), code_challenge_method:'S256'});
    location.assign(`${environment.keycloak.issuer}/protocol/openid-connect/auth?${search}`);
  }

  async completarLogin(code: string, stateRecibido: string): Promise<void> {
    const state = sessionStorage.getItem(PREFIX + 'state');
    const verifier = sessionStorage.getItem(PREFIX + 'verifier');
    sessionStorage.removeItem(PREFIX + 'state');
    sessionStorage.removeItem(PREFIX + 'verifier');
    if (!state || state !== stateRecibido || !verifier) throw new Error('State o code_verifier inválido; inicia sesión otra vez.');
    const body = new URLSearchParams({grant_type:'authorization_code',client_id:environment.keycloak.clientId,
      redirect_uri:environment.keycloak.redirectUri,code,code_verifier:verifier});
    const response = await fetch(`${environment.keycloak.issuer}/protocol/openid-connect/token`, {
      method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body});
    if (!response.ok) throw new Error(`Keycloak no entregó el token (HTTP ${response.status}).`);
    const tokens = await response.json() as {access_token:string; id_token?:string; refresh_token?:string; expires_in:number};
    if (!tokens.access_token) throw new Error('Falta el access_token.');
    this.sesion.set({accessToken:tokens.access_token, idToken:tokens.id_token ?? '',
      refreshToken:tokens.refresh_token ?? '', expiresAt:Date.now() + tokens.expires_in * 1000});
  }
  destinoPendiente(): string {
    const d = sessionStorage.getItem(PREFIX + 'destino') ?? '/';
    sessionStorage.removeItem(PREFIX + 'destino');
    return d.startsWith('/') && !d.startsWith('//') && !d.startsWith('/auth/callback') ? d : '/';
  }
  cerrarSesion(): void {
    const id = this.sesion()?.idToken;
    this.sesion.set(null);
    const params = new URLSearchParams({client_id: environment.keycloak.clientId,
      post_logout_redirect_uri:environment.keycloak.postLogoutRedirectUri});
    if (id) params.set('id_token_hint', id);
    location.assign(`${environment.keycloak.issuer}/protocol/openid-connect/logout?${params}`);
  }
}
