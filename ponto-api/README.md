# ⏳ Ponto API - Sistema de Gestão de Ponto Multi-Tenant

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring"/>
  <img src="https://img.shields.io/badge/Java-17+-007396?style=for-the-badge&logo=openjdk"/>
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens"/>
</p>

## 📜 Descrição do Projeto

O **Ponto API** é o backend de um sistema de gestão de ponto escalável e moderno, desenvolvido em **Spring Boot**.

Seu principal diferencial é a arquitetura **Multi-Tenant (Multi-Inquilino)**, onde os dados de cada empresa cliente (Tenant) são isolados em seus próprios bancos de dados (schema/database), garantindo segurança e escalabilidade, enquanto o gerenciamento dos usuários e metadados é feito em um banco de dados mestre.

## ✨ Funcionalidades

O `ponto-api` fornece os endpoints (portas de acesso) para as seguintes funcionalidades:

* **Autenticação JWT:** Login de funcionários, RH e administradores.
* **Bater Ponto:** Registro de eventos de ponto (`ENTRADA`, `SAIDA`, `INTERVALO`) de forma segura.
* **Workflow de Edição:** Solicitação de edição de ponto pelo funcionário (`PENDENTE_EDICAO`).
* **Gestão de Pendências (RH):** Listagem, aprovação e rejeição de solicitações de edição de ponto.
* **Relatórios Multi-Tenant:** Geração de relatórios de resumo diário por empresa cliente.

## 🛠️ Tecnologias Utilizadas

* **Backend:** Java 17+ (LTS)
* **Framework:** Spring Boot 3+
* **Persistência:** Spring Data JPA / Hibernate
* **Banco de Dados:** PostgreSQL (principal)
* **Segurança:** Spring Security (JWT - JSON Web Token)
* **Contêineres:** Podman/Docker (para o ambiente de desenvolvimento)

---

## 🚀 Como Rodar o Projeto Localmente

### Pré-requisitos

Você precisará ter instalado em sua máquina:

* **Java JDK 17** ou superior.
* **Maven** (Gerenciador de dependências).
* **PostgreSQL** (Rodando localmente, geralmente na porta `5432`) ou **Podman/Docker**.

### 1. Configuração do Banco de Dados

1.  **Crie o Banco Mestre:** Crie um banco de dados PostgreSQL chamado `gestor_empresas`.
2.  **Crie os Bancos de Clientes (Opcional para iniciar):** Crie os bancos `empresa_tecnova` e `empresa_alpha` (ou use a lógica de criação de esquema/banco de dados que definimos).
3.  **Configurações de Conexão:** Altere o arquivo `src/main/resources/application.properties` com as credenciais do seu PostgreSQL:

    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/gestor_empresas
    spring.datasource.username=seu_usuario_postgres
    spring.datasource.password=sua_senha_postgres
    ```

### 2. Execução

1.  **Clone o Repositório:**
    ```bash
    git clone [SUA_URL_DO_REPOSITORIO]
    cd ponto-api
    ```
2.  **Compile e Execute (Maven):**
    ```bash
    # Para compilar o projeto
    mvn clean install
    # Para rodar a aplicação
    mvn spring-boot:run
    ```

A API estará acessível em `http://localhost:8080`.

---

## 🔑 Endpoints Principais (Exemplos)

| Método | URL | Descrição | Requisito de Login |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Autentica o usuário e retorna o **JWT**. | Público |
| `POST` | `/api/punches/event` | Registra uma batida de ponto (`ENTRADA`, `SAIDA`). | Funcionário Logado |
| `GET` | `/api/rh/punches/pending` | Lista todas as pendências de edição da empresa. | ROLE\_RH |
| `GET` | `/api/reports/{id}/daily-summary` | Gera o resumo diário de uma empresa (Tenant). | ROLE\_RH
