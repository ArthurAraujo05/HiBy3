import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router'; // <-- Importa o Roteador

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet], // <-- Diz ao Angular para usar o Roteador
  templateUrl: './app.component.html', // <-- Aponta para o HTML (renomeado)
  styleUrl: './app.component.scss' // <-- Aponta para o SCSS (renomeado)
})
export class AppComponent {
  title = 'ponto-web';
}