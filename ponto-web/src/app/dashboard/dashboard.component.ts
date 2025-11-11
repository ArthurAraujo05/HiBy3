// Arquivo: src/app/dashboard/dashboard.component.ts (O "Cérebro" das Tarefas do RH)

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../services/auth.service'; // Precisamos do token

// 1. Definição do Tipo (DTO) que o Backend está enviando
interface PendingPunchDTO {
  punchId: number;
  employeeName: string;
  eventType: string;
  reason: string;
  originalTimestamp: string;
  requestedTimestamp: string;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule], // O HttpClient já é provido globalmente
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  
  // Nossas variáveis de estado
  public pendingTasks: PendingPunchDTO[] = [];
  public isLoading: boolean = true;
  public errorMessage: string | null = null;

  // URLs da nossa API de RH
  private apiBaseUrl = 'http://localhost:8080/api/rh/punches';

  // Injeção de dependência moderna
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  // 2. ngOnInit é o "construtor" que roda quando o componente carrega
  ngOnInit(): void {
    this.fetchPendingTasks();
  }

  /**
   * 3. Busca a lista de tarefas pendentes do backend.
   */
  public fetchPendingTasks(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // A chamada de API (o Interceptor vai adicionar o token automaticamente)
    this.http.get<PendingPunchDTO[]>(`${this.apiBaseUrl}/pending`).subscribe({
      
      next: (data) => {
        this.pendingTasks = data;
        this.isLoading = false;
        console.log('Tarefas pendentes carregadas:', data);
      },
      
      error: (err) => {
        // O RestExceptionHandler do Backend vai nos dar um JSON limpo
        this.errorMessage = err.error?.message || 'Falha ao carregar tarefas.';
        this.isLoading = false;
      }
    });
  }

  /**
   * 4. Ações (Aprovar ou Rejeitar)
   */
  public handleApproval(punchId: number, action: 'approve' | 'reject'): void {
    
    // Desabilita o botão para evitar cliques duplos
    this.isLoading = true; 
    const apiUrl = `${this.apiBaseUrl}/${action}/${punchId}`;

    this.http.put(apiUrl, {}).subscribe({
      
      next: () => {
        console.log(`Ação ${action} para o ID ${punchId} foi bem-sucedida.`);
        // Sucesso! Remove a tarefa da lista da tela
        this.pendingTasks = this.pendingTasks.filter(task => task.punchId !== punchId);
        this.isLoading = false;
      },
      
      error: (err) => {
        console.error(`Falha ao ${action}`, err);
        this.errorMessage = err.error?.message || `Falha ao processar a ${action}.`;
        this.isLoading = false;
      }
    });
  }
}