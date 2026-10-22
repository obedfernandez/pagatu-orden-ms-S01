import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Producto } from './producto.model';
@Injectable({providedIn:'root'}) export class ProductoService {
  private http=inject(HttpClient);
  listar(){return this.http.get<Producto[]>(`${environment.apiBaseUrl}/api/v1/productos`);}
}
