import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../catalogo/producto-service';
import { Producto } from '../catalogo/producto.model';
import { OrdenService } from './orden-service';
import { OrdenResponse } from './orden.model';
interface ItemCarrito {producto:Producto;cantidad:number;}
@Component({selector:'app-orden-crear',imports:[FormsModule,RouterLink],template:`
<div class="page-head"><div><p class="eyebrow">TIENDA / NUEVA ORDEN</p><h1>Haz tu pedido</h1><p class="subtle">Elige tus productos y confirma el método de pago.</p></div><a routerLink="/mis-pedidos" class="btn btn-outline">Mis pedidos →</a></div>
@if(error()){<p class="alert error">{{error()}}</p>}
@if(resultado(); as r){<div class="panel success-panel"><div class="success-icon">✓</div><p class="eyebrow">ORDEN REGISTRADA</p><h2>Pedido #{{r.id}}</h2><div class="state-pill" [class.is-pending]="r.estado==='PENDIENTE_PAGO'">{{r.estado}}</div>
@if(r.estado==='PENDIENTE_PAGO'){<p>Tu pedido se registró. <b>El pago todavía se está procesando</b> mediante Kafka; no significa que ya esté pagado.</p>}
@else if(r.estado==='CARRITO'){<p>Stock insuficiente; la orden quedó en CARRITO y no se intentó cobrar.</p>}
@else{<p>Este es el estado recibido del servidor. Consulta su evolución en Mis pedidos.</p>}
<div class="hero-actions"><a class="btn btn-primary" routerLink="/mis-pedidos">Seguir estado del pago →</a><button class="btn btn-outline" (click)="nueva()">Crear otra orden</button></div></div>}
@else {<div class="shop-layout"><section class="panel"><div class="panel-title"><h3>Productos disponibles</h3><span class="count-chip">{{productos().length}} productos</span></div>
@if(cargando()){<div class="empty-state">Cargando productos…</div>} @else{<div class="product-grid">
@for(p of productos();track p.id){<article class="product-card"><div class="product-thumb"><span>◈</span><span class="product-category">{{p.categoria?.nombre || 'Catálogo'}}</span></div><div class="product-detail"><h3>{{p.nombre}}</h3><p>{{p.descripcion || 'Producto del catálogo Pagatu'}}</p><div class="product-price">S/ {{p.precio.toFixed(2)}}</div>
<div class="product-actions"><span class="stock" [class.no-stock]="p.stock===0">{{p.stock}} en stock</span><input aria-label="Cantidad" type="number" min="1" [max]="p.stock" value="1" #cantidad><button class="btn btn-primary small" [disabled]="p.stock<1" (click)="agregar(p,cantidad.valueAsNumber)">Agregar +</button></div></div></article>}
@empty{<div class="empty-state">No hay productos activos.</div>}
</div>}</section>
<aside class="panel basket-panel"><div class="panel-title"><h3>Tu carrito</h3><span class="count-chip">{{carrito().length}} ítems</span></div>
@if(carrito().length===0){<div class="basket-empty"><span>◇</span><p>Tu carrito está vacío</p><small>Agrega un producto para comenzar</small></div>}
@else{<div class="basket-items">@for(item of carrito();track item.producto.id){<div class="basket-item"><div><strong>{{item.producto.nombre}}</strong><small>{{item.cantidad}} × S/ {{item.producto.precio.toFixed(2)}}</small></div><button class="remove" (click)="quitar(item.producto.id)" aria-label="Quitar producto">×</button></div>}</div><div class="total-row"><span>Total estimado</span><b>S/ {{total().toFixed(2)}}</b></div>
<label class="field-label" for="metodo">Método de pago</label><select class="input" name="metodo" id="metodo" [(ngModel)]="metodoPago"><option value="YAPE_PLIN">Yape / Plin</option><option value="TARJETA">Tarjeta</option><option value="TRANSFERENCIA">Transferencia</option><option value="TARJETA_RECHAZADA">Tarjeta rechazada (prueba S11)</option></select>
<button class="btn btn-primary btn-block" (click)="confirmar()" [disabled]="enviando()">{{enviando()?'Procesando…':'Confirmar pedido'}} →</button><p class="basket-foot">La confirmación registra la orden; el estado del pago puede tardar unos segundos.</p>}
</aside></div>}
`})
export class OrdenCrear implements OnInit {
  private productosSrv=inject(ProductoService); private ordenesSrv=inject(OrdenService);
  protected readonly productos=signal<Producto[]>([]); protected readonly carrito=signal<ItemCarrito[]>([]);
  protected readonly cargando=signal(true); protected readonly enviando=signal(false);
  protected readonly error=signal<string|null>(null); protected readonly resultado=signal<OrdenResponse|null>(null);
  protected metodoPago='YAPE_PLIN'; protected readonly total=computed(()=>this.carrito().reduce((s,i)=>s+i.producto.precio*i.cantidad,0));
  ngOnInit(){this.productosSrv.listar().subscribe({next:p=>{this.productos.set(p.filter(x=>x.activo));this.cargando.set(false);},error:()=>{this.error.set('No se pudo cargar el catálogo. Comprueba el Gateway y Catálogo MS.');this.cargando.set(false);}});}
  protected agregar(producto:Producto,cantidad:number){if(!Number.isInteger(cantidad)||cantidad<1||producto.stock<1)return;
    this.error.set(null);const actual=this.carrito().find(x=>x.producto.id===producto.id);const nuevo=(actual?.cantidad ?? 0)+cantidad;
    if(nuevo>producto.stock){this.error.set('No puedes agregar más unidades que las disponibles.');return;}
    this.carrito.update(items=>actual?items.map(i=>i.producto.id===producto.id?{...i,cantidad:nuevo}:i):[...items,{producto,cantidad}]);}
  protected quitar(id:number){this.carrito.update(items=>items.filter(i=>i.producto.id!==id));}
  protected nueva(){this.resultado.set(null);this.error.set(null);this.ngOnInit();}
  protected confirmar(){if(this.enviando()||this.carrito().length===0)return;
    this.enviando.set(true);this.error.set(null);this.ordenesSrv.crear({metodoPago:this.metodoPago,
      detalles:this.carrito().map(i=>({idProducto:i.producto.id,cantidad:i.cantidad}))}).subscribe({
        next:orden=>{this.resultado.set(orden);this.carrito.set([]);this.enviando.set(false);},
        error:()=>{this.error.set('No fue posible crear la orden. Revisa tu sesión y los servicios.');this.enviando.set(false);},
      });}
}
