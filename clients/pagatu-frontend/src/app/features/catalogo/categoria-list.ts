import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoriaService } from './categoria-service';
import { Categoria } from './categoria.model';
import { AuthService } from '../../core/auth/auth-service';
@Component({selector:'app-categoria-list',imports:[RouterLink],template:`
  <div class="page-head"><div><p class="eyebrow">CATÁLOGO / GESTIÓN</p><h1>Categorías</h1><p class="subtle">Organiza y consulta las categorías disponibles.</p></div>
  @if (auth.tieneRol('ADMIN')) {<a class="btn btn-primary" routerLink="/categorias/nueva">+ Nueva categoría</a>}</div>
  @if (error()) {<p class="alert error">{{error()}}</p>}
  @if (actionError()) {<p class="alert error">{{actionError()}}</p>}
  <div class="panel"><div class="panel-title"><h3>Listado de categorías</h3><span class="count-chip">{{categorias().length}} registradas</span></div>
  @if (cargando()) {<div class="empty-state">Cargando categorías…</div>}
  @else {<div class="table-wrap"><table><thead><tr><th>Nombre</th><th>Descripción</th><th class="right">Acciones</th></tr></thead><tbody>
  @for (c of categorias(); track c.id) {<tr><td class="name-cell">{{c.nombre}}</td><td>{{c.descripcion || '—'}}</td><td class="right">@if(auth.tieneRol('ADMIN')) {<a class="text-action" [routerLink]="['/categorias', c.id, 'editar']">Editar</a> <button class="text-action danger" (click)="eliminar(c.id!)">Eliminar</button>} @else {<span class="subtle">Solo lectura</span>}</td></tr>}
  @empty {<tr><td colspan="3" class="empty-state">No hay categorías registradas.</td></tr>}
  </tbody></table></div>}</div>`})
export class CategoriaList implements OnInit {
  protected readonly auth = inject(AuthService); private service=inject(CategoriaService);
  protected readonly categorias=signal<Categoria[]>([]); protected readonly cargando=signal(true);
  protected readonly error=signal<string|null>(null); protected readonly actionError=signal<string|null>(null);
  ngOnInit(){this.cargar();}
  private cargar(){this.cargando.set(true); this.service.listar().subscribe({next:d=>{this.categorias.set(d);this.cargando.set(false);},error:()=>{this.error.set('No se pudieron cargar las categorías.');this.cargando.set(false);}});}
  protected eliminar(id:number) { if(!confirm('¿Eliminar esta categoría?')) return; this.actionError.set(null);
    this.service.eliminar(id).subscribe({next:()=>this.cargar(),error:err=>this.actionError.set(err.status===500 ? 'Esta categoría tiene productos asociados y no se puede eliminar.' : 'No se pudo eliminar la categoría.')}); }
}
