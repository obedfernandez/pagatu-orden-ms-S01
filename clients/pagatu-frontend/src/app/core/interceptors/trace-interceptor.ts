import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';
export const traceInterceptor: HttpInterceptorFn = (req, next) =>
  next(req.url.startsWith(environment.apiBaseUrl + '/') ?
    req.clone({setHeaders:{'X-Trace-ID':crypto.randomUUID()}}) : req);
