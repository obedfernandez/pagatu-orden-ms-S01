import { Component, inject, OnInit, signal } from '@angular/core';
import { PagoService } from './pago-service';
import { Pago } from './pago.model';
@Component({selector:'app-pago-list',template:`
<div class="page-head"><div><p class="eyebrow">ADMINISTRACIÓN / PAGOS</p><h1>Todos los pagos</h1><p class="subtle">Vista administrativa. Acceso exclusivo de ADMIN.</p></div></div>
@if(error()){<p class="alert error">{{error()}}</p>}
<div class="panel"><div class="panel-title"><h3>Transacciones</h3><span class="count-chip">{{pagos().length}} pagos</span></div>
@if(cargando()){<div class="empty-state">Cargando pagos…</div>}@else{<div class="table-wrap"><table><thead><tr><th>Pago</th><th>Orden</th><th>Monto</th><th>Método</th><th>Estado</th><th>Fecha</th></tr></thead><tbody>
@for(p of pagos();track p.id){<tr><td class="name-cell">#{{p.id}}</td><td>#{{p.ordenId}}</td><td>S/ {{p.monto.toFixed(2)}}</td><td>{{p.metodoPago}}</td><td><span class="state-pill" [class.paid]="p.estado==='VALIDADO'" [class.cancelled]="p.estado==='FALLIDO'">{{p.estado}}</span></td><td>{{p.fechaPago?.slice(0,19).replace('T',' ')}}</td></tr>}
@empty{<tr><td colspan="6" class="empty-state">No hay pagos registrados.</td></tr>}</tbody></table></div>}</div>`})
export class PagoList implements OnInit {
  private pagoSrv=inject(PagoService); protected readonly pagos=signal<Pago[]>([]);
  protected readonly cargando=signal(true);protected readonly error=signal<string|null>(null);
  ngOnInit(){this.pagoSrv.listar().subscribe({next:d=>{this.pagos.set(d);this.cargando.set(false);},error:()=>{this.error.set('No se pudo cargar el listado de pagos.');this.cargando.set(false);}});}
}
