# Sistema de Gestão de Eventos

Sistema desenvolvido como projeto da pós-graduação para aplicar
organização arquitetural, separação de responsabilidades e evolução
gradual de um monólito modular para uma arquitetura baseada em serviços
independentes.

O projeto evoluiu em quatro etapas: a **Etapa 1** estruturou o monólito
modular; a **Etapa 2** extraiu Notificação para um serviço independente;
a **Etapa 3** adicionou configuração externa, profiles, Spring Cloud
Config Server, bancos independentes e containerização; e a **Etapa 4**
introduziu comunicação assíncrona com RabbitMQ e processamento em lote
com Spring Batch.

------------------------------------------------------------------------

## 1. Objetivo

O sistema permite administrar eventos, participantes, inscrições e
notificações. A evolução foi feita progressivamente para preservar as
regras de negócio enquanto novas responsabilidades arquiteturais foram
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
                  +-----------+-----------+
                  |                       |
                  v                       v
          sistema-eventos ───────> notificacao-service
                  |                       |
                  v                       v
          postgres-eventos        postgres-notificacao

Etapa 4
                         Config Server
                              |
                  +-----------+-----------+
                  |                       |
                  v                       v
          sistema-eventos ──HTTP──> notificacao-service
                  |                       |
                  |                       v
                  |                postgres-notificacao
                  |
                  +── mensagem ──> RabbitMQ ──> notificacao-service
                  |
                  +── Spring Batch
                        |
                        v
              CSV de participantes
                        |
                        v
                 postgres-eventos
```

------------------------------------------------------------------------

## 2. Tecnologias utilizadas

-   Java 25
-   Spring Boot 4.1.1
-   Spring Web MVC
-   Spring Data JPA
-   Spring Cloud OpenFeign
-   Spring Cloud Config
-   Spring AMQP
-   RabbitMQ
-   Spring Batch
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

A Etapa 4 mantém as três aplicações da etapa anterior e acrescenta o
RabbitMQ como broker de mensagens, além do processamento em lote dentro
do `sistema-eventos`.

### 3.1 sistema-eventos

Aplicação principal responsável por Eventos, Participantes, Inscrições e
regras de negócio associadas.

Também possui três formas de processamento/comunicação:

-   HTTP/OpenFeign para operações síncronas do serviço de notificações;
-   publicação assíncrona de criação de notificações no RabbitMQ;
-   Spring Batch para importação de participantes por CSV.

``` text
Cliente HTTP
     |
     v
sistema-eventos
     |
     +------ REST / regras de negócio ------> postgres-eventos
     |
     +------ HTTP/OpenFeign ----------------> notificacao-service
     |
     +------ mensagem ----------------------> RabbitMQ
     |
     +------ Spring Batch <----------------- CSV
```

### 3.2 notificacao-service

Serviço independente responsável pelo domínio de notificações e
proprietário do banco `notificacao_db`.

Na Etapa 4, a criação de notificações passa a poder ocorrer de forma
assíncrona. O serviço consome mensagens do RabbitMQ e persiste as
notificações em seu próprio banco.

As consultas e atualizações que necessitam resposta imediata continuam
disponíveis por HTTP/OpenFeign.

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

### 3.4 RabbitMQ

Broker utilizado na Etapa 4 para desacoplar a publicação de uma
notificação do seu processamento.

Fluxo:

``` text
sistema-eventos
      |
      | publica NotificacaoRequest
      v
eventos.notificacao.exchange
      |
      | routing key: eventos.notificacao
      v
eventos.notificacao.queue
      |
      v
notificacao-service
      |
      v
notificacao_db
```

------------------------------------------------------------------------

## 4. Organização por domínio

O `sistema-eventos` continua organizado por funcionalidades de negócio.
Na Etapa 4 foram acrescentados os componentes de mensageria e Batch.

``` text
com.eventos.sistema.sistema_eventos
|
+-- config
|   +-- RabbitMqConfig
|
+-- evento
|   +-- controller
|   +-- dto
|   +-- entity
|   +-- repository
|   +-- service
|
+-- participante
|   +-- batch
|   |   +-- ParticipanteBatchConfig
|   |   +-- ParticipanteBatchController
|   |   +-- ParticipanteCsv
|   |   +-- ParticipanteItemReader
|   |   +-- ParticipanteItemProcessor
|   |   +-- ParticipanteItemWriter
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
|   +-- messaging
|       +-- NotificacaoProducer
|
+-- shared
    +-- exception
    +-- filter
```

No `notificacao-service`, a Etapa 4 acrescenta o `NotificacaoConsumer`,
responsável pelo consumo das mensagens da fila.

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
além das buscas por nome/e-mail e validação de unicidade do e-mail.

Na Etapa 4, participantes também podem ser importados em lote por
arquivo CSV.

### 5.3 Inscrição

Representa o relacionamento entre Evento e Participante:

``` text
Evento 1 -------- N Inscrição N -------- 1 Participante
```

Responsabilidades:

-   criar inscrições;
-   verificar se o evento está aberto;
-   verificar capacidade;
-   impedir inscrições duplicadas;
-   cancelar e reativar inscrições;
-   consultar por evento e participante.

Status:

-   `ATIVA`
-   `CANCELADA`

### 5.4 Notificação

Na Etapa 1 fazia parte do monólito. Na Etapa 2 foi extraída para o
`notificacao-service`. Na Etapa 4, sua criação passou a utilizar
mensageria assíncrona.

Tipos:

-   `EMAIL`
-   `SISTEMA`

Status:

-   `PENDENTE`
-   `ENVIADA`
-   `FALHA`

------------------------------------------------------------------------

## 6. Comunicação entre serviços

A aplicação utiliza comunicação síncrona e assíncrona, de acordo com a
necessidade da operação.

### 6.1 HTTP / OpenFeign

O `sistema-eventos` continua consumindo operações do
`notificacao-service` por HTTP utilizando Spring Cloud OpenFeign quando
é necessária uma resposta imediata.

Exemplos:

-   consultar notificações;
-   consultar notificação por ID;
-   marcar notificação como enviada;
-   marcar notificação como falha.

No Docker Compose, a comunicação interna utiliza:

``` text
http://notificacao-service:8081
```

### 6.2 Mensageria / RabbitMQ

A criação de uma notificação não precisa bloquear a requisição original
aguardando o processamento completo. Por isso, na Etapa 4 essa operação
foi escolhida para comunicação assíncrona.

``` text
POST /notificacoes
       |
       v
sistema-eventos
       |
       | NotificacaoProducer
       v
RabbitMQ
       |
       | eventos.notificacao.queue
       v
NotificacaoConsumer
       |
       v
NotificacaoService
       |
       v
notificacao_db
```

O endpoint retorna `202 Accepted`, indicando que a solicitação foi
aceita para processamento assíncrono.

A mensagem contém somente as informações necessárias ao processamento:

-   `participanteId`;
-   `tipo`;
-   `mensagem`.

------------------------------------------------------------------------

## 7. Comportamento em indisponibilidade do consumer

Uma característica importante da mensageria foi validada durante os
testes.

Com o `notificacao-service` temporariamente indisponível, uma mensagem
foi publicada pelo `sistema-eventos`. O RabbitMQ manteve a mensagem na
fila com estado `Ready`.

Quando o consumer voltou a funcionar, a mensagem foi consumida e a
notificação foi persistida.

``` text
Consumer indisponível

sistema-eventos
      |
      v
RabbitMQ
      |
      +--> mensagem aguardando na fila

Consumer disponível novamente

RabbitMQ
      |
      v
notificacao-service
      |
      v
notificacao_db
```

Esse comportamento é diferente de uma chamada REST direta: na chamada
síncrona, a indisponibilidade do serviço remoto impede a conclusão da
comunicação naquele momento; com o broker, a mensagem pode permanecer
aguardando processamento.

------------------------------------------------------------------------

## 8. Bancos de dados

São utilizados dois bancos PostgreSQL independentes.

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

Cada serviço é proprietário de sua persistência. O `sistema-eventos` não
acessa diretamente as tabelas do `notificacao-service`.

------------------------------------------------------------------------

## 9. Liquibase

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

O Hibernate do projeto principal utiliza `ddl-auto: validate`.

------------------------------------------------------------------------

## 10. Profiles e variáveis de ambiente

Foram definidos os profiles:

-   `dev`
-   `prod`

O profile ativo é controlado por:

``` text
SPRING_PROFILES_ACTIVE
```

Principais configurações externalizadas:

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
CONFIG_SERVER_URL
SERVICO_NOTIFICACAO_URL
SERVER_PORT
SPRING_PROFILES_ACTIVE
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
```

No ambiente `dev`, valores locais podem ser utilizados como defaults. No
Docker Compose, os nomes dos serviços da rede Docker são fornecidos
pelas variáveis de ambiente.

Exemplo do RabbitMQ:

``` yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

No Compose:

``` yaml
RABBITMQ_HOST: rabbitmq
RABBITMQ_PORT: 5672
RABBITMQ_USERNAME: guest
RABBITMQ_PASSWORD: guest
```

------------------------------------------------------------------------

## 11. Spring Cloud Config Server

Os clientes utilizam:

``` yaml
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

O Config Server centraliza configurações de `dev` e `prod`, incluindo
URLs e portas dependentes do ambiente.

No ambiente Docker, os clientes acessam:

``` text
http://config-server:8888
```

------------------------------------------------------------------------

## 12. Spring Batch

A funcionalidade escolhida para processamento em lote foi a **importação
de participantes por arquivo CSV**.

Essa operação é adequada ao Batch porque trabalha com um conjunto de
registros que deve seguir a mesma sequência de leitura,
validação/transformação e persistência.

### 12.1 Arquivo de entrada

Arquivo:

``` text
src/main/resources/batch/participantes.csv
```

Exemplo:

``` csv
nome,email,telefone
Joao Silva,joao.batch@email.com,21999999999
Maria Souza,maria.batch@email.com,21988888888
Carlos Santos,carlos.batch@email.com,21977777777
```

### 12.2 Fluxo

``` text
participantes.csv
       |
       v
ItemReader
       |
       v
ParticipanteCsv
       |
       v
ItemProcessor
       |
       v
Participante
       |
       v
ItemWriter
       |
       v
ParticipanteRepository
       |
       v
postgres-eventos
```

### 12.3 Job e Step

O Job utilizado é:

``` text
importarParticipantesJob
```

O Step é:

``` text
importarParticipantesStep
```

O Step utiliza processamento orientado a chunks:

``` text
chunk(2)
```

Isso significa que os itens são processados em grupos, em vez de todo o
arquivo ser carregado e persistido de uma única vez.

### 12.4 ItemReader

O `ParticipanteItemReader` utiliza `FlatFileItemReader` para ler o CSV,
ignorando o cabeçalho e convertendo cada linha em `ParticipanteCsv`.

### 12.5 ItemProcessor

O `ParticipanteItemProcessor` aplica regras antes da persistência:

-   remove espaços desnecessários;
-   converte o e-mail para minúsculas;
-   normaliza os valores;
-   verifica se o e-mail já existe;
-   filtra registros duplicados.

Quando o e-mail já existe, o processor retorna `null`, fazendo com que o
registro seja filtrado e não chegue ao writer.

### 12.6 ItemWriter

O `ParticipanteItemWriter` recebe os participantes processados e utiliza
o `ParticipanteRepository` para persistir o chunk no banco
`sistema_eventos`.

### 12.7 Execução manual

A execução automática do Job durante a inicialização foi desabilitada:

``` yaml
spring:
  batch:
    job:
      enabled: false
```

O Job pode ser disparado pelo endpoint:

``` text
POST /batch/participantes/importar
```

Cada execução recebe um parâmetro `timestamp`, permitindo novas
instâncias do Job.

Resposta esperada:

``` text
202 Accepted
```

Durante os testes, o Job foi concluído com:

``` text
status: COMPLETED
```

------------------------------------------------------------------------

## 13. REST x Mensageria x Batch

As três abordagens possuem finalidades diferentes no projeto.

### REST

Adequado quando o cliente ou outro serviço precisa de uma resposta
imediata.

No projeto:

-   CRUD de eventos;
-   CRUD de participantes;
-   inscrições;
-   consultas de notificações;
-   atualização do status de notificações.

### Mensageria

Adequada quando o processamento pode ocorrer de forma assíncrona e o
produtor não precisa aguardar o consumer concluir o trabalho.

No projeto:

-   criação assíncrona de notificações.

### Batch

Adequado quando é necessário processar um conjunto de registros seguindo
uma sequência estruturada.

No projeto:

-   importação de participantes a partir de CSV.

Resumo:

``` text
REST       -> requisição/resposta imediata
Messaging  -> comunicação assíncrona entre componentes
Batch      -> processamento estruturado de conjuntos de dados
```

------------------------------------------------------------------------

## 14. Docker

Cada aplicação possui Dockerfile próprio:

``` text
sistema-eventos/Dockerfile
notificacao-service/Dockerfile
config-server/Dockerfile
```

Imagens utilizadas na Etapa 4:

``` text
sistema-eventos:etapa-4
notificacao-service:etapa-4
config-server:etapa-3
rabbitmq:4-management-alpine
postgres:16-alpine
```

O Config Server não precisou de alteração funcional na Etapa 4, portanto
sua imagem permaneceu na versão construída na Etapa 3.

------------------------------------------------------------------------

## 15. Docker Compose

O ambiente integrado da Etapa 4 contém seis containers:

``` text
config-server
postgres-eventos
postgres-notificacao
rabbitmq
notificacao-service
sistema-eventos
```

Todos participam da rede:

``` text
eventos-network
```

### Portas

  Serviço                   Host   Container
  ---------------------- ------- -----------
  sistema-eventos           8080        8080
  notificacao-service       8081        8081
  config-server             8888        8888
  RabbitMQ AMQP             5672        5672
  RabbitMQ Management      15672       15672
  postgres-eventos          5433        5432
  postgres-notificacao      5434        5432

### Volumes

``` text
postgres-eventos-data
postgres-notificacao-data
rabbitmq-data
```

`docker compose down` encerra os containers sem apagar os volumes.

### Healthchecks

Foram configurados healthchecks para:

-   Config Server;
-   PostgreSQL de eventos;
-   PostgreSQL de notificações;
-   RabbitMQ.

------------------------------------------------------------------------

## 16. Execução com Docker Compose

### Pré-requisitos

-   Docker Desktop;
-   Docker Compose;
-   Java 25;
-   Maven.

### Gerar os JARs

Nos projetos alterados:

``` bash
mvn clean package -DskipTests
```

### Construir as imagens

No `sistema-eventos`:

``` bash
docker build --no-cache -t sistema-eventos:etapa-4 .
```

No `notificacao-service`:

``` bash
docker build --no-cache -t notificacao-service:etapa-4 .
```

O Config Server continua utilizando:

``` text
config-server:etapa-3
```

### Iniciar

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
rabbitmq               Up (healthy)
notificacao-service    Up
sistema-eventos        Up
```

### Encerrar

``` bash
docker compose down
```

------------------------------------------------------------------------

## 17. Swagger

Interface da aplicação principal:

``` text
http://localhost:8080/swagger-ui/index.html
```

O Swagger permite testar os endpoints REST, a publicação assíncrona de
notificações e o disparo manual do Job de importação.

------------------------------------------------------------------------

## 18. Principais endpoints

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

``` text
POST   /notificacoes
GET    /notificacoes
GET    /notificacoes/{id}
PATCH  /notificacoes/{id}/enviada
PATCH  /notificacoes/{id}/falha
```

O `POST /notificacoes` publica a mensagem no RabbitMQ e retorna
`202 Accepted`.

### Batch

``` text
POST /batch/participantes/importar
```

Dispara manualmente o `importarParticipantesJob`.

------------------------------------------------------------------------

## 19. Validação e tratamento de exceções

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

## 20. Testes e validação integrada da Etapa 4

A Etapa 4 foi validada localmente e no ambiente Docker Compose.

### Mensageria

Foram validados:

-   publicação pelo `sistema-eventos`;
-   criação da mensagem no RabbitMQ;
-   chegada da mensagem à fila;
-   consumo pelo `notificacao-service`;
-   persistência no `notificacao_db`;
-   retorno `202 Accepted`;
-   consulta posterior da notificação;
-   consumer temporariamente indisponível;
-   mensagem permanecendo `Ready` no broker;
-   processamento da mensagem após o consumer voltar.

### Spring Batch

Foram validados:

-   leitura do `participantes.csv`;
-   execução do `ItemReader`;
-   execução do `ItemProcessor`;
-   execução do `ItemWriter`;
-   processamento em `chunk(2)`;
-   persistência dos três participantes;
-   filtro de e-mails já existentes;
-   disparo manual pelo Swagger;
-   execução dentro do Docker Compose;
-   Job finalizado com status `COMPLETED`.

### Ambiente integrado

Foram verificados os seis containers:

``` text
config-server
postgres-eventos
postgres-notificacao
rabbitmq
notificacao-service
sistema-eventos
```

------------------------------------------------------------------------

## 21. Evolução arquitetural

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

### Etapa 4 --- Comunicação assíncrona e processamento em lote

Foram acrescentados RabbitMQ e Spring Batch.

``` text
                          Config Server
                               |
                    +----------+----------+
                    |                     |
                    v                     v
             sistema-eventos ------> notificacao-service
                    |       HTTP            |
                    |                       v
                    |               postgres-notificacao
                    |
                    +----> RabbitMQ --------+
                    |
                    +----> Spring Batch
                              |
                              v
                      postgres-eventos
```

------------------------------------------------------------------------

## 22. Reflexões da Etapa 3

### 22.1 Quais configurações variam entre ambientes?

As principais são URL, usuário e senha dos bancos, portas, URL do
serviço de notificações, profile ativo, endereço do Config Server,
configurações do RabbitMQ e comportamento de exibição/formatação de SQL.

### 22.2 Quais configurações foram externalizadas?

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
SERVICO_NOTIFICACAO_URL
CONFIG_SERVER_URL
SERVER_PORT
SPRING_PROFILES_ACTIVE
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
```

### 22.3 Por que um serviço não deve acessar diretamente o banco de outro serviço?

Cada serviço deve ser proprietário de seus dados e regras. O acesso
direto criaria forte acoplamento com o schema interno do outro serviço.
A comunicação por contratos HTTP ou mensagens permite que cada serviço
evolua sua implementação e persistência de forma independente.

### 22.4 Qual problema o Docker resolve?

O Docker empacota a aplicação e seu ambiente de execução em uma imagem
reproduzível, reduzindo diferenças entre máquinas e ambientes.

### 22.5 Qual é a função do Docker Compose?

O Docker Compose coordena aplicações, bancos, Config Server, RabbitMQ,
rede, volumes, variáveis de ambiente, healthchecks e dependências de
inicialização.

### 22.6 Qual problema a configuração centralizada resolve?

O Config Server cria um ponto central para propriedades dependentes de
ambiente, facilitando manutenção, padronização e separação entre código
e configuração.

------------------------------------------------------------------------

## 23. Reflexões da Etapa 4

### 23.1 Qual operação foi escolhida para execução assíncrona?

Foi escolhida a criação de notificações. O `sistema-eventos` publica uma
mensagem no RabbitMQ e o `notificacao-service` é responsável por
consumi-la e persistir a notificação.

### 23.2 Por que essa operação não precisa ser concluída durante a requisição original?

A aplicação principal não precisa aguardar a persistência da notificação
para continuar o fluxo. É suficiente confirmar que a solicitação foi
aceita para processamento. Por isso, o endpoint retorna `202 Accepted`.

Isso reduz o acoplamento temporal entre produtor e consumidor.

### 23.3 O que acontece se o consumer estiver indisponível?

A mensagem permanece armazenada na fila do RabbitMQ aguardando um
consumer disponível.

Esse comportamento foi testado desligando temporariamente o
`notificacao-service`. A mensagem permaneceu `Ready` na fila e foi
processada quando o serviço voltou.

### 23.4 Qual funcionalidade foi escolhida para processamento em lote?

Foi escolhida a importação de participantes por arquivo CSV.

O arquivo contém múltiplos participantes que são lidos, processados e
persistidos no banco da aplicação principal.

### 23.5 Por que essa funcionalidade é adequada para Spring Batch?

A importação trabalha com um conjunto de registros que segue a mesma
sequência:

``` text
ler -> validar/normalizar -> filtrar -> persistir
```

O Spring Batch fornece uma estrutura apropriada para esse tipo de
processamento por meio de Job, Step, ItemReader, ItemProcessor,
ItemWriter e chunks.

### 23.6 Quando utilizar REST, mensageria e Batch nesta aplicação?

**REST** deve ser utilizado quando é necessária interação síncrona e
resposta imediata, como CRUD, consultas e atualizações.

**Mensageria** deve ser utilizada quando um componente pode solicitar um
processamento sem aguardar sua conclusão, como a criação de
notificações.

**Batch** deve ser utilizado quando existe um conjunto de dados a ser
processado de maneira estruturada, como a importação de participantes
por CSV.

------------------------------------------------------------------------

## 24. Repositórios

-   Sistema de Eventos: `https://github.com/thiagoss86/sistema-eventos`
-   Serviço de Notificações:
    `https://github.com/thiagoss86/notificacao-service`
-   Config Server: `https://github.com/thiagoss86/config-server`

------------------------------------------------------------------------

## 25. Status

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
-   [x] Commit final da Etapa 3
-   [x] Tag Git `etapa-3`

### Etapa 4

-   [x] RabbitMQ
-   [x] Producer
-   [x] Exchange, Queue e Routing Key
-   [x] Consumer no `notificacao-service`
-   [x] Criação assíncrona de notificações
-   [x] Teste com consumer indisponível
-   [x] Persistência da mensagem até retorno do consumer
-   [x] Spring Batch
-   [x] Job e Step
-   [x] ItemReader
-   [x] ItemProcessor
-   [x] ItemWriter
-   [x] Processamento em chunks
-   [x] Importação de participantes por CSV
-   [x] Filtro de e-mails duplicados
-   [x] Endpoint manual para execução do Job
-   [x] Teste integrado via Docker Compose
-   [x] Job finalizado com `COMPLETED`
-   [x] Documentação
-   [x] Commit final da Etapa 4
-   [x] Tag Git `etapa-4`

------------------------------------------------------------------------

## 26. Situação atual

A implementação funcional da **Etapa 4 --- Comunicação Assíncrona e
Processamento em Lote** está concluída e validada.

Restam apenas o commit final e a criação da tag Git `etapa-4`.
