# Agendamento Notificação API

Uma API REST estruturada para o gerenciamento e automatização de agendamentos com foco em notificações. O projeto foi desenvolvido utilizando astack do Java e ecossistema Spring, priorizando uma arquitetura limpa, separação de responsabilidades e alta manutenibilidade.

## 🚀 Tecnologias Utilizadas

*   **Java 21** (ou a versão utilizada no seu ambiente)
*   **Spring Boot 3.x**
    *   Spring Web (Construção de APIs REST)
    *   Spring Data JPA (Persistência de dados)
*   **Apache Maven** (Gerenciador de dependências)

---

## 📁 Estrutura de Pastas e Arquitetura

O projeto adota uma divisão arquitetural bem definida para isolar o domínio de negócio dos detalhes de infraestrutura:

```text
src/main/java/com/java/agendamento_...
│
├── 🧠 business         # Regras de negócio e lógica central do sistema
│   ├── mapper         # Conversão de dados (Entidades ↔ DTOs)
│   └── AgendamentoService.java
│
├── 🎛️ controller       # Camada de exposição da API (Endpoints REST)
│   ├── dto            # Objetos de transferência de dados (Input)
│   ├── out            # Objetos de transferência de dados (Output)
│   └── AgendamentoController.java
│
└── ⚙️ infrastructure   # Detalhes técnicos, persistência e integrações
    ├── entities       # Modelagem das tabelas do banco de dados
    ├── enums          # Tipos enumerados de domínio
    ├── exception      # Manipulação global de erros e exceções da API
    └── repositories   # Interfaces de comunicação com o banco de dados (JPA)

    🛠️ Como Executar o Projeto
Pré-requisitos
JDK 21 instalado.
Maven instalado.
