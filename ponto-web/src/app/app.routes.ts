// ARQUIVO: src/app/app.routes.ts
import { inject } from '@angular/core';
import { Routes, Router } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component'; // <--- IMPORTA A CLASSE
import { LayoutComponent } from './dashboard/layout/layout.component';
import { AuthService } from './services/auth.service';

// O AuthGuard (Função que verifica a permissão)
const authGuard = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isAuthenticated()) {
        return true;
    }
    return router.parseUrl('/login');
};

export const routes: Routes = [
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },

    // A ROTA PRINCIPAL AGORA USA O LAYOUTCOMPONENT COMO PAI
    { 
        path: 'dashboard', 
        component: LayoutComponent,
        canActivate: [authGuard],
        children: [
            // A rota vazia ("") é o conteúdo principal
            { path: '', component: DashboardComponent } // <--- ROTA VÁLIDA
        ]
    }
];