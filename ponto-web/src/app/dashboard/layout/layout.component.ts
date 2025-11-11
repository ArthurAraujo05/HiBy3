// ARQUIVO: src/app/dashboard/layout/layout.component.ts (CORRIGIDO)
import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service'; 

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive], 
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {

  // Injeta o AuthService (que tem o método logout)
  constructor(private authService: AuthService) { }

  logout(): void {
    this.authService.logout(); // Chama o método de logout
  }
}