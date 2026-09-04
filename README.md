# Sistema de Gestão de Eventos

Sistema desenvolvido como projeto da pós-graduação com o objetivo de aplicar conceitos de organização arquitetural, separação de responsabilidades e evolução de uma aplicação monolítica para uma arquitetura baseada em serviços independentes.

Neste primeiro estágio, a aplicação permanece como um único projeto Spring Boot, porém organizada internamente em módulos de negócio bem definidos.

---

## 1. Objetivo do Projeto

O Sistema de Gestão de Eventos permite administrar eventos, participantes, inscrições e notificações.

A aplicação foi estruturada inicialmente como um monólito modular, preparando sua evolução para uma arquitetura baseada em microserviços nas etapas posteriores do projeto.

O sistema contempla:

- Cadastro e gerenciamento de eventos;
- Cadastro e gerenciamento de participantes;
- Inscrição de participantes em eventos;
- Controle de capacidade dos eventos;
- Cancelamento e reativação de inscrições;
- Registro e gerenciamento de notificações.

---

## 2. Tecnologias Utilizadas

Nesta etapa foram utilizadas as seguintes tecnologias:

- Java 25
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Liquibase
- Bean Validation
- Lombok
- Springdoc OpenAPI / Swagger
- Maven
- JUnit 5
- Mockito

Tecnologias previstas para as próximas etapas não fazem parte da implementação atual.

---

## 3. Arquitetura

A aplicação utiliza uma arquitetura monolítica modular.

Apesar de todos os módulos estarem executando dentro da mesma aplicação Spring Boot, cada domínio possui suas próprias responsabilidades e componentes.

Fluxo principal:

    Cliente HTTP
         |
         v
    Controller
         |
         v
      Service
         |
         v
     Repository
         |
         v
     PostgreSQL

### Responsabilidades

#### Controller

Responsável exclusivamente pela comunicação HTTP.

Suas responsabilidades incluem:

- Receber requisições;
- Validar os dados de entrada através de Bean Validation;
- Encaminhar as requisições para o Service;
- Retornar as respostas HTTP.

Os Controllers não acessam diretamente os Repositories.

#### Service

Responsável pelas regras de negócio da aplicação.

Exemplos:

- Validar se um evento está aberto;
- Verificar capacidade de um evento;
- Impedir inscrições duplicadas;
- Validar alterações e cancelamentos;
- Controlar estados das notificações.

#### Repository

Responsável pelo acesso aos dados através do Spring Data JPA.

As consultas ao banco são realizadas pelos Repositories, mantendo o acesso à persistência separado das regras de negócio.

---

## 4. Organização por Domínio

A aplicação foi organizada por funcionalidades de negócio, e não apenas por camadas técnicas.

Estrutura principal:

    com.eventos.sistema.sistema_eventos
    |
    +-- evento
    |   +-- controller
    |   +-- dto
    |   +-- entity
    |   +-- repository
    |   +-- service
    |
    +-- participante
    |   +-- controller
    |   +-- dto
    |   +-- entity
    |   +-- repository
    |   +-- service
    |
    +-- inscricao
    |   +-- controller
    |   +-- dto
    |   +-- entity
    |   +-- repository
    |   +-- service
    |
    +-- notificacao
    |   +-- controller
    |   +-- dto
    |   +-- entity
    |   +-- repository
    |   +-- service
    |
    +-- shared
        +-- exception
        |   +-- dto
        |
        +-- filter

Essa organização permite que cada módulo mantenha seus componentes relacionados próximos, facilitando manutenção e futura separação em serviços independentes.

---

# 5. Módulos

## 5.1 Evento

Responsável pelo gerenciamento dos eventos.

Principais responsabilidades:

- Criar eventos;
- Consultar eventos;
- Atualizar eventos;
- Cancelar eventos;
- Controlar o status do evento;
- Controlar a capacidade;
- Validar o período do evento.

### Status

- `ABERTO`
- `ENCERRADO`
- `CANCELADO`

---

## 5.2 Participante

Responsável pelo gerenciamento das pessoas participantes dos eventos.

Principais responsabilidades:

- Criar participantes;
- Consultar participantes;
- Atualizar participantes;
- Excluir participantes;
- Buscar por nome;
- Buscar por e-mail;
- Garantir que o e-mail seja único.

---

## 5.3 Inscrição

Responsável pelo relacionamento entre participantes e eventos.

A inscrição foi modelada como uma entidade própria porque possui informações específicas, como data da inscrição e status.

Relacionamento:

    Evento 1 -------- N Inscrição N -------- 1 Participante

Principais responsabilidades:

- Criar inscrições;
- Verificar se o evento está aberto;
- Verificar a capacidade disponível;
- Impedir inscrições duplicadas;
- Cancelar inscrições;
- Reativar inscrições canceladas;
- Consultar inscrições por evento;
- Consultar inscrições por participante.

### Status

- `ATIVA`
- `CANCELADA`

Quando uma inscrição cancelada é realizada novamente, a inscrição existente é reativada, evitando a criação de registros duplicados.

---

## 5.4 Notificação

Responsável pelo registro e gerenciamento das notificações destinadas aos participantes.

Nesta primeira etapa, o módulo não realiza o envio efetivo de mensagens. Ele mantém o registro das notificações e seus respectivos estados.

Principais responsabilidades:

- Criar notificações;
- Consultar notificações;
- Buscar notificações por participante;
- Buscar notificações por status;
- Buscar notificações por tipo;
- Registrar notificações enviadas;
- Registrar falhas no processamento.

### Tipos

- `EMAIL`
- `SISTEMA`

### Status

- `PENDENTE`
- `ENVIADA`
- `FALHA`

---

# 6. Dependências entre Módulos

Existe uma dependência funcional entre os módulos.

O módulo de Inscrição depende dos módulos de Evento e Participante.

Para realizar uma inscrição, o sistema precisa:

1. Localizar o evento;
2. Verificar se o evento está aberto;
3. Verificar a capacidade disponível;
4. Localizar o participante;
5. Verificar se já existe uma inscrição;
6. Criar ou reativar a inscrição.

Representação:

    Inscrição
       |
       +----> Evento
       |
       +----> Participante

Essa dependência está concentrada no `InscricaoService`, mantendo o Controller responsável apenas pela comunicação HTTP.

---

# 7. Candidato a Futuro Microserviço

O módulo escolhido como candidato à extração para um serviço independente é o módulo de **Notificação**.

## Responsabilidade

O futuro serviço será responsável pelo gerenciamento e processamento das notificações destinadas aos participantes.

## Motivo da escolha

O módulo de Notificação possui uma responsabilidade relativamente independente do núcleo principal de gerenciamento de eventos.

Sua função pode evoluir para processos específicos de comunicação, como envio de e-mails e processamento assíncrono.

Além disso, o módulo possui uma fronteira de negócio clara, facilitando sua futura transformação em um serviço independente.

## Situação atual

Nesta etapa, Notificação continua dentro da mesma aplicação Spring Boot.

A separação será realizada somente na etapa posterior do projeto.

---

# 8. Banco de Dados

O projeto utiliza PostgreSQL como banco de dados relacional.

O banco utilizado pela aplicação é:

    sistema_eventos

A criação do banco é realizada manualmente.

A criação e alteração das tabelas é controlada pelo Liquibase.

As entidades atuais são:

- `eventos`
- `participantes`
- `inscricoes`
- `notificacoes`

---

# 9. Liquibase

O Liquibase é utilizado para controlar a evolução do schema do banco de dados.

Estrutura:

    src/main/resources
    |
    +-- application.yml
    |
    +-- db
        +-- changelog
            +-- db.changelog-master.yaml
            |
            +-- changes
                +-- 001-criacao-tabela-eventos.yaml
                +-- 002-criacao-tabela-participantes.yaml
                +-- 003-criacao-tabela-inscricoes.yaml
                +-- 004-criacao-tabela-notificacoes.yaml
                +-- 005-renomear-coluna-tipo-notificacao.yaml

O Hibernate está configurado com:

    ddl-auto: validate

Dessa forma, o Hibernate valida a estrutura existente sem criar ou alterar tabelas automaticamente.

---

# 10. Validação

A aplicação utiliza Bean Validation nos DTOs de entrada.

Entre as validações utilizadas estão:

- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Email`
- `@Min`

Exemplos de regras:

- Nome obrigatório;
- E-mail válido;
- Capacidade maior que zero;
- Datas obrigatórias;
- Mensagens de notificação obrigatórias;
- IDs de evento e participante obrigatórios.

---

# 11. Tratamento de Exceções

As exceções de negócio são centralizadas através de um `@RestControllerAdvice`.

Foram criadas exceções específicas para:

- Recurso não encontrado;
- Violação de regra de negócio.

Os erros de validação também são tratados de forma centralizada.

As respostas de erro possuem informações como:

- Data/hora;
- Status HTTP;
- Descrição do erro;
- Identificador da requisição.

A aplicação também utiliza o `RequestStatusFilter` para gerar um identificador único para cada requisição.

Esse identificador é retornado no header:

    X-Request-Status-Id

Isso facilita a identificação de requisições durante troubleshooting.

---

# 12. Consultas Spring Data JPA

Além dos métodos fornecidos pelo `JpaRepository`, foram implementadas consultas derivadas específicas.

### Evento

    findByNomeContainingIgnoreCase(String nome)

    findByStatus(StatusEvento status)

### Participante

    findByNomeContainingIgnoreCase(String nome)

    findByEmailIgnoreCase(String email)

    existsByEmailIgnoreCase(String email)

### Inscrição

    findByEventoIdAndParticipanteId(Long eventoId, Long participanteId)

    findByEventoId(Long eventoId)

    findByParticipanteId(Long participanteId)

    countByEventoIdAndStatus(Long eventoId, StatusInscricao status)

### Notificação

    findByParticipanteId(Long participanteId)

    findByStatus(StatusNotificacao status)

    findByTipo(TipoNotificacao tipo)

---

# 13. Documentação da API

A API REST é documentada utilizando Springdoc OpenAPI.

Após iniciar a aplicação, a interface Swagger pode ser acessada em:

    http://localhost:8080/swagger-ui/index.html

Os endpoints estão organizados por módulo:

- Eventos;
- Participantes;
- Inscrições;
- Notificações.

Os principais endpoints possuem descrição de suas operações e responsabilidades.

---

# 14. Principais Endpoints

## Eventos

    POST   /eventos
    GET    /eventos
    GET    /eventos/{id}
    GET    /eventos/buscar/nome
    GET    /eventos/buscar/status
    PUT    /eventos/{id}
    PATCH  /eventos/{id}/cancelar

## Participantes

    POST   /participantes
    GET    /participantes
    GET    /participantes/{id}
    GET    /participantes/buscar/nome
    GET    /participantes/buscar/email
    PUT    /participantes/{id}
    DELETE /participantes/{id}

## Inscrições

    POST   /inscricoes
    GET    /inscricoes
    GET    /inscricoes/{id}
    GET    /inscricoes/buscar/evento/{eventoId}
    GET    /inscricoes/buscar/participante/{participanteId}
    PATCH  /inscricoes/{id}/cancelar

## Notificações

    POST   /notificacoes
    GET    /notificacoes
    GET    /notificacoes/{id}
    GET    /notificacoes/buscar/participante/{participanteId}
    GET    /notificacoes/buscar/status
    GET    /notificacoes/buscar/tipo
    PATCH  /notificacoes/{id}/enviada
    PATCH  /notificacoes/{id}/falha

---

# 15. Execução do Projeto

## Pré-requisitos

- Java 25
- Maven
- PostgreSQL

## Banco de dados

Criar o banco:

    CREATE DATABASE sistema_eventos;

As tabelas serão criadas automaticamente pelo Liquibase durante a inicialização da aplicação.

## Configuração

A conexão com o banco deve ser configurada em:

    src/main/resources/application.yml

Exemplo:

    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/sistema_eventos
        username: postgres
        password: SUA_SENHA

## Executando

Utilizando Maven:

    ./mvnw spring-boot:run

No Windows:

    mvnw.cmd spring-boot:run

Após a inicialização:

    http://localhost:8080

Swagger:

    http://localhost:8080/swagger-ui/index.html

---

# 16. Testes

Foram realizados testes das principais regras e operações da aplicação.

Entre os cenários testados estão:

- Criação de eventos;
- Validação de dados;
- Busca de eventos;
- Cancelamento de eventos;
- Criação de participantes;
- E-mail duplicado;
- Busca de participantes;
- Exclusão de participantes;
- Criação de inscrições;
- Inscrição duplicada;
- Limite de capacidade;
- Cancelamento de inscrição;
- Reativação de inscrição;
- Criação de notificações;
- Consulta de notificações;
- Alteração de status;
- Tratamento de regras de negócio.

---

# 17. Evolução Arquitetural

A aplicação foi construída nesta primeira etapa como um monólito modular.

Nas próximas etapas, a arquitetura será evoluída gradualmente.

### Etapa 1

Monólito modular:

    Sistema de Eventos
          |
          +-- Evento
          +-- Participante
          +-- Inscrição
          +-- Notificação

### Etapa 2

Extração do módulo de Notificação para um serviço independente.

### Etapa 3

Externalização de configurações, PostgreSQL por serviço e containerização da aplicação.

### Etapa 4

Introdução de comunicação assíncrona e processamento em lote.

---

# 18. Status da Etapa 1

- [x] Organização por domínio
- [x] Módulo Evento
- [x] Módulo Participante
- [x] Módulo Inscrição
- [x] Módulo Notificação
- [x] Separação Controller / Service / Repository
- [x] DTOs
- [x] Bean Validation
- [x] Tratamento centralizado de exceções
- [x] PostgreSQL
- [x] Liquibase
- [x] Consultas Spring Data JPA
- [x] OpenAPI / Swagger
- [x] Identificação das dependências entre módulos
- [x] Definição do candidato a futuro microserviço
- [x] Tag Git `etapa-1`