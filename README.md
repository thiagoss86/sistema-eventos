# Sistema de Gestão de Eventos

Sistema desenvolvido como projeto da pós-graduação para aplicar
organização arquitetural, separação de responsabilidades e evolução
gradual de um monólito modular para uma arquitetura baseada em serviços
independentes.

O projeto evoluiu em três etapas: a **Etapa 1** estruturou o monólito
modular; a **Etapa 2** extraiu Notificação para um serviço independente;
e a **Etapa 3** adicionou configuração externa, profiles, Spring Cloud
Config Server, bancos independentes e containerização.

------------------------------------------------------------------------

## 1. Objetivo

O sistema permite administrar eventos, participantes, inscrições e
notificações. A evolução foi feita progressivamente para preservar as
regras de negócio enquanto as responsabilidades arquiteturais foram
separadas.

### Evolução

``` text
Etapa 1
Sistema de Eventos
├── Evento
├── Participante
├── Inscrição
└── Notificação

Etapa 2
sistema-eventos ──HTTP/OpenFeign──> notificacao-service

Etapa 3
                         Config Server
                              |
                 +------------+------------+
                 |                         |
                 v                         v
         sistema-eventos ─────────> notificacao-service
                 |                         |
                 v                         v
         postgres-eventos          postgres-notificacao
```

------------------------------------------------------------------------

## 2. Tecnologias utilizadas

-   Java 25
-   Spring Boot 4.1.1
-   Spring Web MVC
-   Spring Data JPA
-   Spring Cloud OpenFeign
-   Spring Cloud Config
-   PostgreSQL 16
-   Liquibase
-   Bean Validation
-   Lombok
-   Springdoc OpenAPI / Swagger
-   Maven
-   JUnit 5
-   Mockito
-   Docker
-   Docker Compose

------------------------------------------------------------------------

## 3. Arquitetura atual

A Etapa 3 é composta por três aplicações.

### 3.1 sistema-eventos

Aplicação principal responsável por Eventos, Participantes, Inscrições e
pelas regras de negócio associadas. Também realiza a integração HTTP com
o serviço de notificações.

O fluxo interno segue:

``` text
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
```

### 3.2 notificacao-service

Serviço independente responsável pelo domínio de notificações. Possui
persistência própria no banco `notificacao_db`.

O `sistema-eventos` não acessa suas tabelas diretamente. A comunicação
ocorre pela API HTTP do serviço.

### 3.3 config-server

Aplicação Spring Cloud Config Server responsável por fornecer
configurações centralizadas para os ambientes `dev` e `prod`.

``` text
config-server
└── src/main/resources/config
    ├── sistema-eventos-dev.yaml
    ├── sistema-eventos-prod.yaml
    ├── notificacao-service-dev.yaml
    └── notificacao-service-prod.yaml
```

------------------------------------------------------------------------

## 4. Organização por domínio

O `sistema-eventos` continua organizado por funcionalidades de negócio:

``` text
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
|   +-- client
|   +-- dto
|   +-- integration
|
+-- shared
    +-- exception
    +-- filter
```

Após a extração da Etapa 2, a aplicação principal mantém no domínio de
notificação somente os componentes necessários para integração com o
serviço independente.

------------------------------------------------------------------------

## 5. Domínios

### 5.1 Evento

Responsável por criar, consultar, atualizar e cancelar eventos,
controlar capacidade, validar período e controlar o status.

Status:

-   `ABERTO`
-   `ENCERRADO`
-   `CANCELADO`

### 5.2 Participante

Responsável por criar, consultar, atualizar e excluir participantes,
além das buscas por nome/e-mail e da validação de unicidade do e-mail.

### 5.3 Inscrição

Representa o relacionamento entre Evento e Participante:

``` text
Evento 1 -------- N Inscrição N -------- 1 Participante
```

Responsabilidades:

-   Criar inscrições;
-   Verificar se o evento está aberto;
-   Verificar capacidade;
-   Impedir inscrições duplicadas;
-   Cancelar e reativar inscrições;
-   Consultar por evento e participante.

Status:

-   `ATIVA`
-   `CANCELADA`

### 5.4 Notificação

Na Etapa 1 fazia parte do monólito. Na Etapa 2 foi extraída para o
`notificacao-service`.

Tipos:

-   `EMAIL`
-   `SISTEMA`

Status:

-   `PENDENTE`
-   `ENVIADA`
-   `FALHA`

------------------------------------------------------------------------

## 6. Comunicação entre serviços

O `sistema-eventos` consome o `notificacao-service` por HTTP utilizando
Spring Cloud OpenFeign.

``` text
Cliente
   |
   v
sistema-eventos
   |
   | OpenFeign / HTTP
   v
notificacao-service
   |
   v
notificacao_db
```

A URL do serviço é externalizada. No Docker Compose, a comunicação
interna utiliza:

``` text
http://notificacao-service:8081
```

Não é utilizado `localhost` para comunicação entre containers.

Quando o serviço remoto está indisponível, a integração trata a falha e
a aplicação principal retorna uma resposta HTTP de indisponibilidade do
serviço.

------------------------------------------------------------------------

## 7. Bancos de dados

A Etapa 3 utiliza dois bancos PostgreSQL independentes.

### sistema-eventos

Banco:

``` text
sistema_eventos
```

Armazena Eventos, Participantes e Inscrições.

### notificacao-service

Banco:

``` text
notificacao_db
```

Armazena exclusivamente Notificações.

``` text
sistema-eventos                 notificacao-service
      |                                  |
      v                                  v
sistema_eventos                   notificacao_db
```

Cada serviço é proprietário de sua persistência.

------------------------------------------------------------------------

## 8. Liquibase

O `sistema-eventos` utiliza Liquibase para controlar a evolução do
schema.

``` text
001-criacao-tabela-eventos.yaml
002-criacao-tabela-participantes.yaml
003-criacao-tabela-inscricoes.yaml
004-criacao-tabela-notificacoes.yaml
005-renomear-coluna-tipo-notificacao.yaml
006-remover-tabela-notificacoes.yaml
```

A migration `006` remove a antiga tabela de notificações do banco
principal após a extração dessa responsabilidade.

O Hibernate do projeto principal utiliza `ddl-auto: validate`, validando
a estrutura gerenciada pelo Liquibase sem alterá-la automaticamente.

------------------------------------------------------------------------

## 9. Profiles e variáveis de ambiente

Foram definidos os profiles:

-   `dev`
-   `prod`

O profile ativo é controlado por:

``` text
SPRING_PROFILES_ACTIVE
```

Configurações externalizadas:

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
CONFIG_SERVER_URL
SERVICO_NOTIFICACAO_URL
SERVER_PORT
SPRING_PROFILES_ACTIVE
```

No ambiente `dev`, os arquivos locais permitem defaults para facilitar a
execução local. Exemplo:

``` yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/sistema_eventos}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:admin}

  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

Em `prod`, a conexão com o banco depende das variáveis externas:

``` yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
```

------------------------------------------------------------------------

## 10. Spring Cloud Config Server

Os clientes utilizam:

``` yaml
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

O Config Server centraliza configurações de `dev` e `prod`. Entre elas
estão a URL do serviço de notificações e as portas dos serviços.

Endpoints disponíveis durante a execução local:

``` text
http://localhost:8888/sistema-eventos/dev
http://localhost:8888/sistema-eventos/prod
http://localhost:8888/notificacao-service/dev
http://localhost:8888/notificacao-service/prod
```

No ambiente Docker, os clientes acessam:

``` text
http://config-server:8888
```

------------------------------------------------------------------------

## 11. Docker

Cada aplicação possui Dockerfile próprio:

``` text
sistema-eventos/Dockerfile
notificacao-service/Dockerfile
config-server/Dockerfile
```

Imagens utilizadas:

``` text
sistema-eventos:etapa-3
notificacao-service:etapa-3
config-server:etapa-3
```

A imagem do Config Server inclui `curl` para permitir seu healthcheck
HTTP.

------------------------------------------------------------------------

## 12. Docker Compose

O ambiente integrado contém:

``` text
config-server
postgres-eventos
postgres-notificacao
notificacao-service
sistema-eventos
```

Todos participam da rede:

``` text
eventos-network
```

### Portas

  Serviço                  Host   Container
  ---------------------- ------ -----------
  sistema-eventos          8080        8080
  notificacao-service      8081        8081
  config-server            8888        8888
  postgres-eventos         5433        5432
  postgres-notificacao     5434        5432

### Volumes

``` text
postgres-eventos-data
postgres-notificacao-data
```

`docker compose down` encerra os containers sem apagar os dados
persistidos. A remoção dos volumes exige explicitamente
`docker compose down -v`.

### Healthchecks

Foram configurados healthchecks para:

-   Config Server;
-   PostgreSQL de eventos;
-   PostgreSQL de notificações.

O `notificacao-service` aguarda o Config Server e seu banco ficarem
saudáveis antes de iniciar, evitando a condição de corrida encontrada
durante os testes de inicialização.

------------------------------------------------------------------------

## 13. Execução com Docker Compose

### Pré-requisitos

-   Docker Desktop;
-   Docker Compose;
-   Java 25;
-   Maven.

### Gerar os JARs

Em cada projeto:

``` bash
mvn clean package
```

### Construir as imagens

No `sistema-eventos`:

``` bash
docker build -t sistema-eventos:etapa-3 .
```

No `notificacao-service`:

``` bash
docker build -t notificacao-service:etapa-3 .
```

No `config-server`:

``` bash
docker build -t config-server:etapa-3 .
```

### Iniciar

No diretório que contém `docker-compose.yml`:

``` bash
docker compose up -d
```

### Verificar

``` bash
docker compose ps
```

Resultado esperado:

``` text
config-server          Up (healthy)
postgres-eventos       Up (healthy)
postgres-notificacao   Up (healthy)
notificacao-service    Up
sistema-eventos        Up
```

### Encerrar

``` bash
docker compose down
```

------------------------------------------------------------------------

## 14. Swagger

Interface da aplicação principal:

``` text
http://localhost:8080/swagger-ui/index.html
```

O Swagger permite testar os endpoints da aplicação e operações que
utilizam a integração com o `notificacao-service`.

------------------------------------------------------------------------

## 15. Principais endpoints

### Eventos

``` text
POST   /eventos
GET    /eventos
GET    /eventos/{id}
GET    /eventos/buscar/nome
GET    /eventos/buscar/status
PUT    /eventos/{id}
PATCH  /eventos/{id}/cancelar
```

### Participantes

``` text
POST   /participantes
GET    /participantes
GET    /participantes/{id}
GET    /participantes/buscar/nome
GET    /participantes/buscar/email
PUT    /participantes/{id}
DELETE /participantes/{id}
```

### Inscrições

``` text
POST   /inscricoes
GET    /inscricoes
GET    /inscricoes/{id}
GET    /inscricoes/buscar/evento/{eventoId}
GET    /inscricoes/buscar/participante/{participanteId}
PATCH  /inscricoes/{id}/cancelar
```

### Notificações

As operações de notificação são encaminhadas pelo `sistema-eventos` ao
serviço independente por meio do cliente OpenFeign.

------------------------------------------------------------------------

## 16. Validação e tratamento de exceções

A aplicação utiliza Bean Validation, incluindo:

-   `@NotNull`
-   `@NotBlank`
-   `@Size`
-   `@Email`
-   `@Min`

As exceções são tratadas de forma centralizada com
`@RestControllerAdvice`.

A aplicação principal também utiliza o header:

``` text
X-Request-Status-Id
```

para identificação de requisições durante troubleshooting.

------------------------------------------------------------------------

## 17. Testes

Foram testados cenários relacionados a Eventos, Participantes,
Inscrições e integração com Notificações, incluindo regras de negócio,
validações e indisponibilidade do serviço remoto.

Os testes automatizados podem ser executados com:

``` bash
mvn clean test
```

------------------------------------------------------------------------

## 18. Validação integrada da Etapa 3

O ambiente completo foi validado através do Docker Compose.

Foram verificados:

-   Inicialização dos cinco containers;
-   Healthcheck do Config Server;
-   Healthcheck dos dois PostgreSQL;
-   Carregamento das configurações centralizadas;
-   Profile `dev`;
-   Conexão de cada serviço ao próprio banco;
-   Comunicação HTTP entre os serviços;
-   Persistência através dos volumes;
-   Reinicialização com `docker compose down` e `docker compose up -d`;
-   Consulta de dados persistidos após a reinicialização;
-   Chamada pelo Swagger passando pela integração com o
    `notificacao-service`.

O teste final confirmou que o ambiente integrado sobe e funciona sem
necessidade de reinicialização manual dos containers.

------------------------------------------------------------------------

## 19. Evolução arquitetural

### Etapa 1 --- Monólito modular

``` text
Sistema de Eventos
├── Evento
├── Participante
├── Inscrição
└── Notificação
```

### Etapa 2 --- Serviço independente

``` text
sistema-eventos
       |
       | OpenFeign
       v
notificacao-service
```

### Etapa 3 --- Configuração, persistência e containers

Foram implementados profiles, variáveis de ambiente, Config Server,
banco independente por serviço, Dockerfiles, Docker Compose, volumes,
rede e healthchecks.

``` text
                       Config Server
                            |
               +------------+------------+
               |                         |
               v                         v
       sistema-eventos ---------> notificacao-service
               |                         |
               v                         v
       postgres-eventos          postgres-notificacao
```

### Etapa 4

A próxima etapa prevê a introdução de comunicação assíncrona e
processamento em lote.

------------------------------------------------------------------------

## 20. Reflexões da Etapa 3

### 20.1 Quais configurações variam entre ambientes?

As principais são URL, usuário e senha dos bancos, portas, URL do
serviço de notificações, profile ativo, endereço do Config Server e
comportamento de exibição/formatação de SQL.

Em desenvolvimento, alguns valores possuem defaults locais. Em produção,
valores dependentes do ambiente são fornecidos externamente.

### 20.2 Quais configurações foram externalizadas?

Foram externalizadas:

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
SERVICO_NOTIFICACAO_URL
CONFIG_SERVER_URL
SERVER_PORT
SPRING_PROFILES_ACTIVE
```

Isso evita fixar configurações dependentes do ambiente no código Java.

### 20.3 Por que um serviço não deve acessar diretamente o banco de outro serviço?

Cada serviço deve ser proprietário de seus dados e regras. O acesso
direto criaria forte acoplamento com o schema interno do outro serviço.
Uma alteração de banco poderia quebrar consumidores externos.

A comunicação pela API mantém o contrato entre os serviços e permite que
cada um evolua sua implementação e persistência de forma independente.

### 20.4 Qual problema o Docker resolve?

O Docker empacota a aplicação e seu ambiente de execução em uma imagem
reproduzível. Isso reduz diferenças entre máquinas e ambientes e permite
executar cada componente de forma isolada.

### 20.5 Qual é a função do Docker Compose?

O Docker Compose coordena os vários containers do ambiente. Neste
projeto, define aplicações, bancos, Config Server, rede, volumes,
variáveis de ambiente, healthchecks e dependências de inicialização.

O ambiente completo pode ser iniciado com:

``` bash
docker compose up -d
```

### 20.6 Qual problema a configuração centralizada resolve?

O Config Server evita que cada serviço mantenha isoladamente todas as
configurações dependentes de ambiente. Ele cria um ponto central para
propriedades dos serviços e profiles, facilitando manutenção,
padronização e separação entre código e configuração.

------------------------------------------------------------------------

## 21. Repositórios

-   Sistema de Eventos: `https://github.com/thiagoss86/sistema-eventos`
-   Serviço de Notificações:
    `https://github.com/thiagoss86/notificacao-service`
-   Config Server: `https://github.com/thiagoss86/config-server`

------------------------------------------------------------------------

## 22. Status

### Etapa 1

-   [x] Organização por domínio
-   [x] Evento, Participante, Inscrição e Notificação
-   [x] Controller / Service / Repository
-   [x] DTOs e Bean Validation
-   [x] Tratamento centralizado de exceções
-   [x] PostgreSQL e Liquibase
-   [x] Spring Data JPA
-   [x] OpenAPI / Swagger
-   [x] Tag Git `etapa-1`

### Etapa 2

-   [x] Extração do `notificacao-service`
-   [x] Comunicação HTTP com OpenFeign
-   [x] URL externa do serviço
-   [x] Tratamento de indisponibilidade
-   [x] Testes
-   [x] Tag Git `etapa-2` no projeto principal

### Etapa 3

-   [x] Profiles `dev` e `prod`
-   [x] Variáveis de ambiente
-   [x] Bancos PostgreSQL independentes
-   [x] Spring Cloud Config Server
-   [x] Dockerfiles
-   [x] Docker Compose
-   [x] Rede entre containers
-   [x] Volumes persistentes
-   [x] Healthchecks
-   [x] Teste integrado
-   [x] Persistência após reinicialização
-   [x] Documentação
-   [ ] Commit final da Etapa 3
-   [ ] Tag Git `etapa-3`

------------------------------------------------------------------------

## 23. Próxima etapa

A próxima evolução prevista é a **Etapa 4**, com introdução de
comunicação assíncrona e processamento em lote.
