import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { OrdenRequest, OrdenResponse } from './orden.model';
@Injectable({providedIn:'root'}) export class OrdenService {
  private http=inject(HttpClient); private base=`${environment.apiBaseUrl}/api/v1/ordenes`;
  crear(request:OrdenRequest){return this.http.post<OrdenResponse>(this.base,request);}
  obtener(id:number){return this.http.get<OrdenResponse>(`${this.base}/${id}`);}
  listar(){return this.http.get<OrdenResponse[]>(this.base);}
}
