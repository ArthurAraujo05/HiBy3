// ARQUIVO: src/app/services/auth.service.ts (CORRIGIDO)
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private TOKEN_KEY = 'jwt_token'; 

  constructor(private router: Router) { }

  // Retorna true se houver um token salvo
  isAuthenticated(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY);
    return !!token; 
  }

  // Faz o logout e redireciona para a página de login
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  // Pega o token para ser usado nas chamadas de API (pelo Interceptor)
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}