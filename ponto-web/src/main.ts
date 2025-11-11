// Arquivo: ponto-web/src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component'; // <-- O nome da classe é AppComponent

bootstrapApplication(AppComponent, appConfig) // <-- Inicia com AppComponent e appConfig
  .catch((err) => console.error(err));