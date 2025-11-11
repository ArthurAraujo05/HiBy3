// Arquivo: ponto-web/src/app/login/login.component.ts

import { Component } from '@angular/core';
import { Router } from '@angular/router'; 
import { HttpClient } from '@angular/common/http'; // Para chamar a API
import { FormsModule } from '@angular/forms'; // CRÍTICO para [(ngModel)] e (ngSubmit)
import { CommonModule } from '@angular/common'; 

// DTOs (Interfaces)
interface LoginRequestDTO {
  email: string;
  password: string;
}

interface AuthenticationResponseDTO {
  token: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,     
    FormsModule,       
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  loginData: LoginRequestDTO = {
    email: '',
    password: ''
  };

  errorMessage: string | null = null;
  isLoading: boolean = false;

  private loginApiUrl = 'http://localhost:8080/auth/login';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  public onLogin(): void {
    if (!this.loginData.email || !this.loginData.password) {
      this.errorMessage = 'Por favor, preencha o e-mail e a senha.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    console.log('Tentando logar com:', this.loginData.email);

    // Chama a API do Backend
    this.http.post<AuthenticationResponseDTO>(this.loginApiUrl, this.loginData)
      .subscribe({
        
        // SUCESSO!
        next: (response) => {
          this.isLoading = false;
          console.log('Login bem-sucedido, token recebido:', response.token);
          
          // Salva o "crachá" (Token) no navegador
          localStorage.setItem('jwt_token', response.token);
          
          // Redireciona para o dashboard
          this.router.navigate(['/dashboard']); 
        },

        // FALHA!
        error: (err) => {
          this.isLoading = false;
          console.error('Erro no login:', err);

          // Pega o erro 400 ou 403 do nosso "RestExceptionHandler"
          if (err.status === 400 || err.status === 403 || err.status === 401) {
            this.errorMessage = 'E-mail ou senha inválidos.';
          } else {
            this.errorMessage = 'Erro inesperado no servidor. Tente novamente mais tarde.';
          }
        }
      });
  }
}