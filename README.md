# API de Agendamento de Notificações

API REST para agendar, acompanhar e cancelar notificações enviadas por e-mail e SMS. O projeto foi desenvolvido com Spring Boot e utiliza processamento assíncrono para separar a criação do agendamento da entrega da mensagem.

Atualmente, os provedores de e-mail e SMS funcionam em modo de simulação. Nesse modo, nenhuma mensagem real é enviada: a aplicação registra o processamento no log e gera um identificador fictício do provedor.

O modo `production` habilita integrações isoladas com Resend para e-mail e Twilio para SMS, sem remover o modo simulado. Para ativá-lo, configure `notification.provider.mode=production` e forneça `RESEND_API_KEY`, `RESEND_FROM`, `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN` e `TWILIO_FROM` por variáveis de ambiente. O telefone destinatário deve usar o padrão internacional E.164, por exemplo `+5511999999999`.

## Regra de negócio

Um agendamento contém uma mensagem, uma data futura para envio e pelo menos um destinatário, que pode ser um endereço de e-mail, um telefone ou ambos.

O horário deve ser informado no padrão ISO 8601 com o offset vigente de Brasília, por exemplo `2026-12-30T14:30:00-03:00`, e respeitar uma antecedência mínima de 5 segundos. A aplicação converte o valor para UTC antes de persistir e devolve as datas no fuso `America/Sao_Paulo`.

Para cada canal informado, a aplicação cria uma entrega independente. Assim, um mesmo agendamento pode ter resultados diferentes para e-mail e SMS e assumir estados como agendado, parcialmente enviado, enviado ou cancelado.

Os eventos de envio são persistidos junto com o agendamento por meio do padrão Transactional Outbox. Quando chega o horário programado, um processo interno publica os eventos no RabbitMQ. Cada canal possui sua própria fila, e os consumidores registram as tentativas e atualizam o estado das entregas.

Agendamentos já enviados não podem ser cancelados. Quando um cancelamento é permitido, somente as entregas ainda não enviadas são marcadas como canceladas. Mensagens já processadas ou canceladas são ignoradas pelos consumidores, evitando processamento duplicado.

## Tecnologias

- Java 21
- Spring Boot 3.5
- Spring Web e Bean Validation
- Spring Data JPA e Hibernate
- PostgreSQL
- RabbitMQ
- Maven
- MapStruct e Lombok
- SpringDoc OpenAPI/Swagger UI
- JUnit e Mockito

## Organização do projeto

O código está dividido nas seguintes áreas principais:

- `controller`: exposição da API, contratos de entrada e saída e tratamento das respostas de erro;
- `business`: regras de negócio, publicação da Outbox, consumidores e provedores de notificação;
- `infrastructure`: configurações, entidades, repositórios, enums e exceções da aplicação.

## Pré-requisitos

Antes de iniciar, tenha instalado:

- JDK 21;
- PostgreSQL;
- RabbitMQ;
- Maven, ou utilize o Maven Wrapper incluído no projeto.

## Configuração local

Crie o banco de dados usado pela aplicação:

```sql
CREATE DATABASE db_agendamento;
```

Por padrão, a aplicação espera os seguintes serviços e credenciais:

| Serviço | Configuração padrão |
| --- |-----|
| PostgreSQL | `localhost:5432/db_agendamento` |
| Usuário do PostgreSQL |     |
| Senha do PostgreSQL |     |
| RabbitMQ | `localhost:5672` |
| Usuário do RabbitMQ |     |
| Senha do RabbitMQ |  |

Copie `src/main/resources/application.properties.example` para `src/main/resources/application.properties` e informe as credenciais locais. O arquivo efetivo é ignorado pelo Git e também pode consumir as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME` e `RABBITMQ_PASSWORD`.

Não mantenha credenciais reais versionadas. Em ambientes compartilhados, prefira variáveis de ambiente ou um gerenciador de segredos.

O schema do banco é atualizado automaticamente pelo Hibernate durante a inicialização por meio da propriedade `spring.jpa.hibernate.ddl-auto=update`.

As datas são armazenadas no PostgreSQL como `timestamp with time zone`. Internamente, agendamentos e registros técnicos utilizam UTC para que comparações, publicação e processamento representem sempre o mesmo instante, independentemente do fuso configurado no sistema operacional.

## Como executar

Com PostgreSQL e RabbitMQ ativos, execute na raiz do projeto.

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Também é possível gerar e executar o pacote da aplicação:

```bash
./mvnw clean package
java -jar target/agendamento-notificacao-api-0.0.1-SNAPSHOT.jar
```

A API ficará disponível em `http://localhost:8080`.

## Documentação da API

Com a aplicação em execução, a documentação interativa pode ser acessada em:

```text
http://localhost:8080/swagger-ui.html
```

O contrato OpenAPI em JSON fica disponível em:

```text
http://localhost:8080/v3/api-docs
```

## Executando os testes

No Windows:

```powershell
.\mvnw.cmd test
```

No Linux ou macOS:

```bash
./mvnw test
```

Os testes de contexto utilizam a configuração atual da aplicação. Portanto, mantenha o PostgreSQL disponível e com o banco configurado antes de executar toda a suíte.

## Processamento e tolerância a falhas

A publicação dos eventos pendentes ocorre periodicamente e em lotes configuráveis. O RabbitMQ utiliza confirmação de publicação, filas duráveis e filas de mensagens mortas (DLQ) separadas por canal.

As tentativas de consumo utilizam intervalos progressivos. Quando o processamento falha, a aplicação registra o erro e a tentativa para permitir o acompanhamento do histórico da entrega.

As principais opções de processamento estão em `application.properties`, incluindo frequência da Outbox, tamanho do lote, tempo limite de confirmação, antecedência mínima do agendamento e modo do provedor.
