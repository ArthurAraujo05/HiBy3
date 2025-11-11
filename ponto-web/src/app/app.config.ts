// ARQUIVO: src/app/app.config.ts (CORRIGIDO)
import { ApplicationConfig, inject } from '@angular/core'; 
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes'; 
import { AuthService } from './services/auth.service';

// Funções de Interceptor (Moderno)
const authInterceptor = (req: any, next: any) => { 
    const authService = inject(AuthService); 
    const token = authService.getToken();

    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}` 
        }
      });
    }
    return next(req);
};


export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    
    // Liga o HttpClient e ativa o Interceptor
    provideHttpClient(
      withInterceptors([
        authInterceptor // Usa a função interceptor
      ])
    ),
  ]
};