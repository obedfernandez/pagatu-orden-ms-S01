import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Pago } from './pago.model';
@Injectable({providedIn:'root'}) export class PagoService {
  private http=inject(HttpClient); private base=`${environment.apiBaseUrl}/api/v1/pagos`;
  obtenerPorOrden(id:number){return this.http.get<Pago>(`${this.base}/por-orden/${id}`);}
  listar(){return this.http.get<Pago[]>(this.base);}
}
