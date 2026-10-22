import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth-service';
@Component({selector:'app-inicio',imports:[RouterLink],template:`
  <div class="intro-kicker"><span class="gold-dot"></span> PLATAFORMA DE PEDIDOS · S11</div>
  <section class="hero"><div><div class="hero-eyebrow">COMERCIO DISTRIBUIDO</div><h1>Todo listo para<br><em>tu próxima compra.</em></h1>
  <p>Explora productos, crea órdenes y sigue tus pagos en tiempo real. Tu tienda conectada con Keycloak, Kafka y microservicios.</p>
  <div class="hero-actions"><a class="btn btn-gold" routerLink="/ordenes/nueva">Crear una orden <span>↗</span></a>
  <a class="btn btn-hero-outline" routerLink="/mis-pedidos">Ver mis pedidos <span>→</span></a></div>
  </div><div class="hero-graphic"><div class="orbit orbit-outer"></div><div class="orbit orbit-inner"></div><div class="graphic-center">P</div><div class="float-tag tag-a">✓ &nbsp; PAGOS</div><div class="float-tag tag-b">↗ &nbsp; PEDIDOS</div></div></section>
  <div class="section-heading"><div><div class="eyebrow">EXPLORA PAGATU</div><h2>¿Qué quieres hacer hoy?</h2></div><span class="subtle">Todo en un solo lugar</span></div>
  <div class="feature-grid"><a class="feature-card" routerLink="/categorias"><span class="feature-icon">▦</span><h3>Administrar categorías</h3><p>Explora y gestiona las categorías del catálogo.</p><span class="feature-link">Ver categorías →</span></a>
  <a class="feature-card" routerLink="/ordenes/nueva"><span class="feature-icon">◈</span><h3>Realizar un pedido</h3><p>Elige productos y confirma tu compra en pocos pasos.</p><span class="feature-link">Nueva orden →</span></a>
  <a class="feature-card" routerLink="/mis-pedidos"><span class="feature-icon">▤</span><h3>Seguir tus pedidos</h3><p>Observa pagos aprobados o rechazados, sin recargar.</p><span class="feature-link">Mis pedidos →</span></a></div>
  <p class="foot-hint">Seguridad OAuth 2.0 + PKCE · API Gateway · Consistencia eventual con Kafka</p>
`})
export class Inicio { protected readonly auth = inject(AuthService); }
