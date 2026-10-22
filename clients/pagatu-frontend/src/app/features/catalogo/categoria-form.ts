import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CategoriaService } from './categoria-service';
@Component({selector:'app-categoria-form',imports:[ReactiveFormsModule,RouterLink],template:`
  <div class="page-head"><div><p class="eyebrow">CATÁLOGO / CATEGORÍAS</p><h1>{{id ? 'Editar categoría' : 'Nueva categoría'}}</h1><p class="subtle">Completa los campos y guarda los cambios.</p></div><a routerLink="/categorias" class="btn btn-outline">← Volver</a></div>
  <div class="panel form-panel"><form [formGroup]="form" (ngSubmit)="guardar()">
    <label class="field-label">Nombre <span>*</span></label><input class="input" formControlName="nombre" maxlength="100" placeholder="Nombre de la categoría">
    @if(form.controls.nombre.touched && form.controls.nombre.invalid){<p class="field-error">Introduce un nombre de 1 a 100 caracteres.</p>}
    <label class="field-label">Descripción</label><textarea class="input" rows="4" formControlName="descripcion" maxlength="255" placeholder="Descripción (opcional)"></textarea>
    @if(error()){<p class="alert error">{{error()}}</p>}
    <div class="form-actions"><a routerLink="/categorias" class="btn btn-outline">Cancelar</a><button class="btn btn-primary" type="submit" [disabled]="guardando()">{{guardando()?'Guardando…':'Guardar categoría'}}</button></div>
  </form></div>`})
export class CategoriaForm {
  private route=inject(ActivatedRoute); private router=inject(Router); private service=inject(CategoriaService);
  private fb=inject(FormBuilder); protected id: number|null = null;
  protected readonly error=signal<string|null>(null); protected readonly guardando=signal(false);
  protected readonly form=this.fb.nonNullable.group({nombre:['',[Validators.required,Validators.maxLength(100)]],descripcion:['',Validators.maxLength(255)]});
  constructor(){const value=this.route.snapshot.paramMap.get('id');if(value){const id=Number(value); if(!Number.isInteger(id)||id<1){this.error.set('ID de categoría inválido.');return;}
    this.id=id;this.service.obtener(id).subscribe({next:c=>this.form.patchValue({nombre:c.nombre,descripcion:c.descripcion ?? ''}),error:()=>this.error.set('No se pudo cargar la categoría.')});}}
  protected guardar(){if(this.form.invalid){this.form.markAllAsTouched();return;}this.guardando.set(true);this.error.set(null);
    const payload=this.form.getRawValue();const request=this.id ? this.service.actualizar(this.id,payload):this.service.crear(payload);
    request.subscribe({next:()=>void this.router.navigateByUrl('/categorias'),error:()=>{this.error.set('No se pudo guardar la categoría. Revisa los permisos y los datos.');this.guardando.set(false);}});}
}
