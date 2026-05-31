# Teste de programação - VR Benefícios

Como parte do processo de seleção, gostaríamos que você desenvolvesse um pequeno sistema, para que possamos ver melhor o seu trabalho.

Fique à vontade para criar a partir dos requisitos abaixo. Se algo não ficou claro, pode assumir o que ficar mais claro para você, e, por favor, *documente suas suposições*.

Crie o projeto no seu Github para que possamos ver os passos realizados (por meio dos commits) para a implementação da solução.

Caso sua solução seja aprovada, faremos uma entrevista contigo, e a utilizaremos durante a entrevista.

Se quiser documentar outros detalhes da sua solução (como *design patterns* e boas práticas utilizadas e outras decisões de projeto) pode mandar ver!

# Mini autorizador

A VR processa todos os dias diversas transações de Vale Refeição e Vale Alimentação, entre outras.
De forma breve, as transações saem das maquininhas de cartão e chegam até uma de nossas aplicações, conhecida como *autorizador*, que realiza uma série de verificações e análises. Essas também são conhecidas como *regras de autorização*.

Ao final do processo, o autorizador toma uma decisão, aprovando ou não a transação:
* se aprovada, o valor da transação é debitado do saldo disponível do benefício, e informamos à maquininha que tudo ocorreu bem.
* senão, apenas informamos o que impede a transação de ser feita e o processo se encerra.

Sua tarefa será construir um *mini-autorizador*. Este será uma aplicação Spring Boot com interface totalmente REST que permita:

* a criação de cartões (todo cartão deverá ser criado com um saldo inicial de R$500,00)
* a obtenção de saldo do cartão
* a autorização de transações realizadas usando os cartões previamente criados como meio de pagamento

## Regras de autorização a serem implementadas

Uma transação pode ser autorizada se:
* o cartão existir
* a senha do cartão for a correta
* o cartão possuir saldo disponível

Caso uma dessas regras não ser atendida, a transação não será autorizada.

## Demais instruções

O projeto contém um docker-compose.yml com 1 banco de dados relacional e outro não relacional.
Sinta-se à vontade para utilizar um deles. Se quiser, pode deixar comentado o banco que não for utilizar, mas não altere o que foi declarado para o banco que você selecionou.

Não é necessário persistir a transação. Mas é necessário persistir o cartão criado e alterar o saldo do cartão caso uma transação ser autorizada pelo sistema.

Serão analisados o estilo e a qualidade do seu código, bem como as técnicas utilizadas para sua escrita. Ficaremos felizes também se você utilizar testes automatizados como ferramenta auxiliar de criação da solução.

Também, na avaliação da sua solução, serão realizados os seguintes testes, nesta ordem:

* criação de um cartão
* verificação do saldo do cartão recém-criado
* realização de diversas transações, verificando-se o saldo em seguida, até que o sistema retorne informação de saldo insuficiente
* realização de uma transação com senha inválida
* realização de uma transação com cartão inexistente

Esses testes serão realizados:
* rodando o docker-compose enviado para você
* rodando a aplicação

Para isso, é importante que os contratos abaixo sejam respeitados:

## Contratos dos serviços

### Criar novo cartão
```
Method: POST
URL: http://localhost:8080/cartoes
Body (json):
{
    "numeroCartao": "6549873025634501",
    "senha": "1234"
}
```
#### Possíveis respostas:
```
Criação com sucesso:
   Status Code: 201
   Body (json):
   {
      "senha": "1234",
      "numeroCartao": "6549873025634501"
   } 
-----------------------------------------
Caso o cartão já exista:
   Status Code: 422
   Body (json):
   {
      "senha": "1234",
      "numeroCartao": "6549873025634501"
   } 
```

### Obter saldo do Cartão
```
Method: GET
URL: http://localhost:8080/cartoes/{numeroCartao} , onde {numeroCartao} é o número do cartão que se deseja consultar
```

#### Possíveis respostas:
```
Obtenção com sucesso:
   Status Code: 200
   Body: 495.15 
-----------------------------------------
Caso o cartão não exista:
   Status Code: 404 
   Sem Body
```

### Realizar uma Transação
```
Method: POST
URL: http://localhost:8080/transacoes
Body (json):
{
    "numeroCartao": "6549873025634501",
    "senhaCartao": "1234",
    "valor": 10.00
}
```

#### Possíveis respostas:
```
Transação realizada com sucesso:
   Status Code: 201
   Body: OK 
-----------------------------------------
Caso alguma regra de autorização tenha barrado a mesma:
   Status Code: 422 
   Body: SALDO_INSUFICIENTE|SENHA_INVALIDA|CARTAO_INEXISTENTE (dependendo da regra que impediu a autorização)
```

Desafios (não obrigatórios):
* é possível construir a solução inteira sem utilizar nenhum if. Só não pode usar *break* e *continue*!
* como garantir que 2 transações disparadas ao mesmo tempo não causem problemas relacionados à concorrência?
  Exemplo: dado que um cartão possua R$10.00 de saldo. Se fizermos 2 transações de R$10.00 ao mesmo tempo, em instâncias diferentes da aplicação, como o sistema deverá se comportar?

---

# Solução

## Stack Tecnológica

| Tecnologia | Versão | Papel |
|------------|--------|-------|
| Java | 22 | Linguagem principal |
| Spring Boot | 3.3.1 | Framework de aplicação |
| Spring Data MongoDB | 4.x | Persistência |
| MongoDB | 7.0 | Banco de dados NoSQL |
| Lombok | latest | `@Slf4j` para logging |
| Jakarta Validation | 3.x | Validação de entrada |
| JUnit 5 + Mockito | latest | Testes unitários |
| Docker + Docker Compose | - | Containerização |

---

## Arquitetura

A aplicação segue arquitetura em camadas com responsabilidades bem definidas:

```
HTTP Request
     │
     ▼
┌─────────────────────────────┐
│        Controller           │  Roteamento HTTP, validação @Valid,
│  CardController             │  montagem de ResponseEntity.
│  TransactionController      │  Sem lógica de negócio.
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│         Service             │  Regras de negócio, hashing de senha,
│  CardServiceImpl            │  validação de autorização, debito de saldo.
│  TransactionServiceImpl     │  @Transactional para consistência.
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│        Repository           │  Acesso ao MongoDB via Spring Data.
│  CardRepository             │  CRUD automático (MongoRepository).
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│         MongoDB             │  Coleção: cards
│   Document: Card            │  Campos: cardNumber (PK), password (hash),
│                             │          amount, version (lock otimista)
└─────────────────────────────┘
```

### Estrutura de Pacotes

```
com.vr.miniauthorizer/
├── controller/
│   ├── CardController.java          # POST /cartoes, GET /cartoes/{id}
│   └── TransactionController.java   # POST /transacoes
├── service/
│   ├── CardService.java             # Interface
│   ├── TransactionService.java      # Interface
│   └── impl/
│       ├── CardServiceImpl.java
│       └── TransactionServiceImpl.java
├── repository/
│   └── CardRepository.java          # MongoRepository<Card, String>
├── document/
│   └── Card.java                    # Documento MongoDB com @Version
├── model/
│   ├── CardModel.java               # Record: request/response de cartão
│   └── TransactionModel.java        # Record: request de transação
├── exception/
│   ├── CardException.java           # CardNotFoundException, CardAlreadyExistsException
│   ├── BalanceException.java
│   ├── PasswordException.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice centralizado
└── utils/
    ├── HashUtil.java                # SHA-256 + Base64 para senha
    └── ExceptionMessages.java       # Constantes CARTAO_INEXISTENTE etc.
```

### Tratamento de Exceções

O tratamento é centralizado no `GlobalExceptionHandler` com precedência hierárquica:

```
CardController (local, prioridade maior)
  ├── CardNotFoundException      → 404 (sobrescreve o global para GET /cartoes/{id})
  └── CardAlreadyExistsException → 422 + body original (echo do request)

GlobalExceptionHandler (global)
  ├── CardNotFoundException          → 422 CARTAO_INEXISTENTE (fluxo de transação)
  ├── BalanceException               → 422 SALDO_INSUFICIENTE
  ├── PasswordException              → 422 SENHA_INVALIDA
  ├── OptimisticLockingFailureException → 422 SALDO_INSUFICIENTE (concorrência)
  ├── MethodArgumentNotValidException   → 400 (validação de body)
  └── Exception                        → 500 (erros inesperados)
```

---

## Decisões de Projeto

### MongoDB como banco de dados
O projeto utilizou MongoDB (banco não relacional) como armazenamento. A entidade `Card` é mapeada como documento na coleção `cards`, com `cardNumber` como chave primária (`@Id`).

### Java 22 Records para DTOs
`CardModel` e `TransactionModel` são `record`s Java 22 — imutáveis, com `equals/hashCode/toString` gerados automaticamente. Anotações Jakarta Validation (`@NotBlank`, `@NotNull`, `@Positive`) garantem validação na entrada HTTP.

### Senha protegida com SHA-256
A senha do cartão nunca é armazenada em texto plano. No momento da criação, ela é hashada com SHA-256 + Base64 (`HashUtil`). Na transação, a senha informada é hashada e comparada ao hash armazenado.

### Injeção por construtor
Todas as classes usam constructor injection (sem `@Autowired` em campo), garantindo imutabilidade dos campos `final` e facilitando testes unitários com Mockito.

### Desafio opcional 1 — Sem `if`
A solução foi construída sem nenhuma instrução `if` no código de negócio:

| Situação | Técnica utilizada |
|----------|-------------------|
| Cartão já existe | `Optional.ifPresent(ignored -> { throw ... })` |
| Senha inválida | `Optional.of(bool).filter(b -> b).orElseThrow(...)` |
| Saldo insuficiente | `Optional.of(balance).filter(b -> b.compareTo(amount) >= 0).orElseThrow(...)` |

### Desafio opcional 2 — Concorrência
Proteção contra double-debit via **optimistic locking** com `@Version Long version` no documento `Card`:

```
T1 lê Card(version=0, amount=10.00) ──► valida ✅ ──► save(v=0) → MongoDB grava v=1
T2 lê Card(version=0, amount=10.00) ──► valida ✅ ──► save(v=0) → MongoDB rejeita
                                                              ↳ OptimisticLockingFailureException
                                                              ↳ GlobalExceptionHandler → 422 SALDO_INSUFICIENTE
```

Resultado: apenas uma das transações concorrentes é aprovada. Sem saldo negativo, sem double-debit, sem dependência de replica set.

---

## Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados
- (Opcional, para desenvolvimento local) Java 22 e Maven 3.9+

---

### Opção 1 — Stack completa via Docker Compose (recomendado)

Sobe MongoDB e a aplicação Spring Boot juntos:

```bash
cd src/main/resources/docker
docker compose up --build
```

A aplicação estará disponível em `http://localhost:8080`.

Para parar e remover os containers:

```bash
docker compose down
```

Para parar preservando os dados do MongoDB:

```bash
docker compose stop
```

---

### Opção 2 — MongoDB no Docker + aplicação local

**1. Subir apenas o MongoDB:**

```bash
cd src/main/resources/docker
docker compose up mongo
```

**2. Em outro terminal, na raiz do projeto, executar a aplicação:**

```bash
./mvnw spring-boot:run
```

A aplicação conecta ao MongoDB em `localhost:27017` por padrão.

---

### Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `MONGODB_URI` | `mongodb://user:password@localhost:27017/miniautorizador` | URI de conexão ao MongoDB |
| `VALUES_STANDARD_VALUE` | `500.00` | Saldo inicial de cada cartão criado |

---

### Executar os Testes

```bash
./mvnw test
```

> Os testes unitários (controller e service) não requerem MongoDB. O `TransactionControllerTest` usa `@SpringBootTest` e requer uma instância do MongoDB em execução.

---

## Exemplos de Uso

### Criar cartão

```bash
curl -X POST http://localhost:8080/cartoes \
  -H "Content-Type: application/json" \
  -d '{"numeroCartao": "6549873025634501", "senha": "1234"}'
```

Resposta `201`:
```json
{"senha": "1234", "numeroCartao": "6549873025634501"}
```

### Consultar saldo

```bash
curl http://localhost:8080/cartoes/6549873025634501
```

Resposta `200`:
```
500.00
```

### Realizar transação

```bash
curl -X POST http://localhost:8080/transacoes \
  -H "Content-Type: application/json" \
  -d '{"numeroCartao": "6549873025634501", "senhaCartao": "1234", "valor": 10.00}'
```

Resposta `201`:
```
OK
```

---

## Checklist

### Requisitos Obrigatórios

- [x] Criação de cartão com saldo inicial de R$500,00
- [x] Retorno 201 com body `{"senha", "numeroCartao"}` ao criar
- [x] Retorno 422 com body original ao tentar criar cartão duplicado
- [x] Consulta de saldo: 200 com valor ou 404 sem body
- [x] Autorização de transação: 201 com `OK`
- [x] Rejeição por cartão inexistente: 422 `CARTAO_INEXISTENTE`
- [x] Rejeição por senha inválida: 422 `SENHA_INVALIDA`
- [x] Rejeição por saldo insuficiente: 422 `SALDO_INSUFICIENTE`
- [x] Cartão persistido no MongoDB após criação
- [x] Saldo decrementado após transação autorizada
- [x] Transação não é persistida
- [x] Docker Compose com MongoDB funcional

### Desafios Opcionais

- [x] Solução construída sem nenhuma instrução `if` no código de negócio
- [x] Proteção contra concorrência via optimistic locking (`@Version` no documento `Card`)

### Qualidade de Código

- [x] Java 22 com Records para DTOs
- [x] Injeção de dependência via construtor (zero `@Autowired` em campo)
- [x] Tratamento de exceções centralizado (`@RestControllerAdvice`)
- [x] JavaDoc completo em todas as classes e métodos públicos
- [x] Logging estruturado com SLF4J (`key=value`)
- [x] Validação de entrada com Jakarta Validation
- [x] Testes unitários com JUnit 5 + Mockito
- [x] Nomes de testes no padrão `should[Behavior]When[Condition]`
- [x] Dockerfile multi-stage (builder Java 22 + runtime JRE mínimo)

---

## Desenvolvedor

**Emerson Lima**
GitHub: [github.com/Emersondll](https://github.com/Emersondll)
