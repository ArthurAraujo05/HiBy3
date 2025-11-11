// Arquivo: src/app/services/auth-interceptor.service.ts
import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service'; // <-- Caminho correto

@Injectable({
  providedIn: 'root'
})
export class AuthInterceptorService implements HttpInterceptor {

  // O construtor injeta o AuthService, não o AuthInterceptor
  constructor(private authService: AuthService) {} 

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Agora, this.authService tem o método getToken()
    const token = this.authService.getToken(); 

    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}` 
        }
      });
    }

    return next.handle(request);
  }
}