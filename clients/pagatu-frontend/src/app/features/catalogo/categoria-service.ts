import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Categoria } from './categoria.model';
@Injectable({providedIn:'root'}) export class CategoriaService {
  private http = inject(HttpClient); private base = `${environment.apiBaseUrl}/api/v1/categorias`;
  listar() { return this.http.get<Categoria[]>(this.base); }
  obtener(id:number) { return this.http.get<Categoria>(`${this.base}/${id}`); }
  crear(categoria:Categoria) { return this.http.post<Categoria>(this.base,categoria); }
  actualizar(id:number,categoria:Categoria) { return this.http.put<Categoria>(`${this.base}/${id}`,categoria); }
  eliminar(id:number) { return this.http.delete<void>(`${this.base}/${id}`); }
}
