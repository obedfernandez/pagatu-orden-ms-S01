import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OrdenService } from './orden-service';
import { PagoService } from './pago-service';
import { AuthService } from '../../core/auth/auth-service';
import { OrdenResponse } from './orden.model';
import { Pago } from './pago.model';
interface PedidoConPago {orden:OrdenResponse;pago:Pago|null;}
@Component({selector:'app-mis-pedidos',imports:[RouterLink],template:`
<div class="page-head"><div><p class="eyebrow">TU ACTIVIDAD / ÓRDENES</p><h1>Mis pedidos</h1><p class="subtle">Consulta tus compras y sigue el estado de los pagos.</p></div><a class="btn btn-primary" routerLink="/ordenes/nueva">+ Nueva orden</a></div>
<div class="info-banner"><span class="pulse-dot"></span><p><strong>Actualización automática activa.</strong> Los pedidos pendientes se consultan cada 4 segundos y cambian de estado sin recargar esta página.</p></div>
@if(error()){<p class="alert error">{{error()}}</p>}
<div class="panel"><div class="panel-title"><h3>Historial de pedidos</h3><span class="count-chip">{{pedidos().length}} pedidos</span></div>
@if(cargando()){<div class="empty-state">Cargando tus pedidos…</div>}
@else if(!pedidos().length){<div class="empty-state"><div class="empty-icon">▤</div>No tienes pedidos todavía.<br><a routerLink="/ordenes/nueva">Haz tu primera compra →</a></div>}
@else{<div class="table-wrap"><table><thead><tr><th>Orden</th><th>Fecha</th><th>Total</th><th>Estado</th><th>Pago</th></tr></thead><tbody>
@for(p of pedidos();track p.orden.id){<tr><td class="name-cell">#{{p.orden.id}}</td><td>{{p.orden.fechaCreacion.slice(0,19).replace('T',' ')}}</td><td>{{p.orden.total===null?'—':'S/ '+p.orden.total.toFixed(2)}}</td><td><span class="state-pill" [class.paid]="p.orden.estado==='PAGADA'" [class.cancelled]="p.orden.estado==='CANCELADA'" [class.is-pending]="p.orden.estado==='PENDIENTE_PAGO'">{{etiqueta(p.orden.estado)}}</span></td><td>@if(p.pago){<strong>{{p.pago.estado}}</strong><small class="subtle payment-method">{{p.pago.metodoPago}}</small>} @else{<span class="subtle">En espera / sin pago</span>}</td></tr>}
</tbody></table></div>}</div>`})
export class MisPedidos implements OnInit,OnDestroy {
  private ordenes=inject(OrdenService); private pagos=inject(PagoService); private auth=inject(AuthService);
  protected readonly pedidos=signal<PedidoConPago[]>([]);protected readonly cargando=signal(true);protected readonly error=signal<string|null>(null);
  private timer?:ReturnType<typeof setInterval>; private busy=false; private solicitudesActivas = new Set<number>();
  ngOnInit(){this.cargar();this.timer=setInterval(()=>this.poll(),4000);}
  ngOnDestroy(){if(this.timer)clearInterval(this.timer);}
  private cargar(){this.ordenes.listar().subscribe({next:list=>{const mias=list.filter(o=>o.idCliente===this.auth.idCliente()).sort((a,b)=>b.id-a.id);
    this.pedidos.set(mias.map(orden=>({orden,pago:null})));this.cargando.set(false);
    for(const item of mias){if(item.estado!=='PENDIENTE_PAGO' && item.estado!=='CARRITO')this.cargarPago(item.id);}},
    error:()=>{this.error.set('No se pudieron consultar tus pedidos.');this.cargando.set(false);}});}
  private poll(){if(this.busy)return;const pendiente=this.pedidos().filter(p=>p.orden.estado==='PENDIENTE_PAGO');if(!pendiente.length)return;
    this.busy=true;let restantes=pendiente.length;
    for(const item of pendiente){this.ordenes.obtener(item.orden.id).subscribe({next:orden=>{if(orden.idCliente!==this.auth.idCliente())return;
      this.pedidos.update(current=>current.map(p=>p.orden.id===orden.id?{...p,orden}:p));
      if(orden.estado!=='PENDIENTE_PAGO')this.cargarPago(orden.id);},
      error:()=>{this.error.set('Hubo un error al actualizar un pedido. Se intentará de nuevo.');if(--restantes===0)this.busy=false;},
      complete:()=>{if(--restantes===0)this.busy=false;}});}}
  private cargarPago(id:number){if(this.solicitudesActivas.has(id))return;this.solicitudesActivas.add(id);
    this.pagos.obtenerPorOrden(id).subscribe({next:pago=>this.pedidos.update(items=>items.map(p=>p.orden.id===id?{...p,pago}:p)),
      error:e=>{if(e.status!==404)this.error.set('No se pudo consultar el detalle de algún pago.');this.solicitudesActivas.delete(id);},
      complete:()=>this.solicitudesActivas.delete(id)});}
  protected etiqueta(e:string){return e==='PENDIENTE_PAGO'?'Procesando pago…':e==='PAGADA'?'Pagada':e==='CANCELADA'?'Cancelada':e==='CARRITO'?'Sin stock':e;}
}
