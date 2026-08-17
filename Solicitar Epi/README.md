# MaterialFlow

Microserviço em **Spring Boot** para o fluxo de solicitação de material entre
unidades de uma empresa: o colaborador pede o que está faltando, a gestora decide
item por item se **transfere de outro estoque** ou se **compra**, e o pedido se
encerra sozinho quando nada fica pendente.

Roda com **H2 em memória** e já vem com dados de exemplo — não é preciso instalar
banco nem configurar nada:

```bash
./mvnw spring-boot:run
```

Depois abra <http://localhost:8085>.

---

## A demonstração

A tela abre com um seletor de papel no canto superior direito. É o mesmo sistema
mostrando telas diferentes conforme o cargo de quem entrou:

| Papel | O que vê |
|---|---|
| **Ana Souza** — solicitante | catálogo só da unidade dela, formulário de pedido e os pedidos que ela abriu |
| **Marcos Lima** — gestor de ST | pedidos de todas as unidades, com as ações de resolver cada item |

Alternando entre os dois dá para acompanhar o ciclo completo: Ana pede, Marcos
transfere um item do estoque da matriz e marca outro como comprado, e o pedido
fecha automaticamente.

No sistema real quem define o papel é a sessão do usuário logado — aqui o botão
existe só para tornar o fluxo visível sem precisar de dois logins.

---

## O fluxo de negócio

**1. Pedido.** O colaborador escolhe materiais do catálogo da própria unidade e
informa a quantidade. Cada item nasce com status `ABERTO`.

**2. Decisão, item por item.** A gestora resolve cada item de duas formas:

- **Transferir** — escolhe de qual produto do estoque dela a quantidade vai sair.
  A baixa na origem e o crédito no destino acontecem **na mesma transação**, e a
  operação é recusada quando o saldo é insuficiente. O estoque nunca fica
  inconsistente pela metade.
- **Comprar** — o item é marcado como `COMPRADO`. O estoque **não** sobe agora:
  isso só acontece quando a mercadoria chega e entra pela nota fiscal. Marcar
  como comprado material que ainda não existe fisicamente seria criar saldo falso.

**3. Encerramento automático.** Quando nenhum item continua `ABERTO`, o pedido
passa a `FINALIZADO`. A API recusa fechar antes disso.

**4. Aviso por e-mail.** Cada pedido novo dispara um e-mail para a gestora. Na
demonstração o envio vem desligado (`hhtec.email.ativo=false`); a falha de e-mail
nunca derruba o pedido, que já está gravado.

---

## Endpoints

```
POST  /solicitarEpi/solicitar                       abre um pedido
GET   /solicitarEpi/listarRegiao?regiao=X            catálogo de uma unidade
POST  /solicitarEpi/transferir?idProdutoOrigem=&idItem=   dá baixa e credita
POST  /solicitarEpi/mudarStatusProduto               marca o item como comprado
POST  /solicitarEpi/mudarFinalizado                  fecha, se nada estiver aberto
GET   /usuario/{id}/solicitacoes                     pedidos de um solicitante
GET   /gestor/{id}/solicitacoes                      todos os pedidos (valida o cargo)
```

Console do banco em <http://localhost:8085/h2-console> (usuário `sa`, senha em branco).

---

## Tecnologias

| Camada | Stack |
|---|---|
| Back-end | Java 25, Spring Boot 4, Spring Data JPA, Bean Validation |
| Banco | H2 em memória na demonstração; MySQL em produção |
| E-mail | Spring Mail (SMTP) |
| Front-end | HTML, CSS e JavaScript sem framework; animação 3D com three.js |
| Build | Maven |

---

## Estrutura

```
src/main/java/Solicitacao/Material/Hhtec/
├── Controller/      endpoints REST
├── Service/         regras de negócio
├── Repository/      acesso a dados (Spring Data)
├── Entity/          entidades JPA
├── Dto/             objetos de entrada
├── Enum/            Status e StatusProduto
├── Email/           montagem e envio dos avisos
└── Exceptions/      exceções de negócio + handler global

src/main/resources/
├── static/          a tela da demonstração
├── data.sql         dados de exemplo (dois usuários, catálogo em duas unidades)
└── application.properties
```

---

## Decisões que valem explicar

**Validação na entrada.** O DTO usa Bean Validation (`@NotNull`, `@NotEmpty`), então
pedido sem item ou sem data é recusado antes de chegar à regra de negócio.

**Tratamento centralizado de exceções.** Um `@RestControllerAdvice` traduz as
exceções de negócio em respostas coerentes: **403** para falta de permissão, **404**
para registro inexistente — em vez de devolver 500 genérico para tudo.

**Transferência transacional.** O método é `@Transactional` e valida o saldo antes
de cada baixa. Numa transferência de vários itens, o saldo da origem já vem
descontado das voltas anteriores, então o total do pedido também não fura o estoque.

**O schema não é gerado pela aplicação em produção.** Lá o `ddl-auto` fica em `none`,
porque o banco é compartilhado com outro sistema — deixar o Hibernate alterar as
tabelas dele já causou perda de `DEFAULT` uma vez. Alteração de schema é feita por
script versionado.

---

## Observações

Este repositório é a versão de **demonstração**. Em produção um
`application.properties` externo, ao lado do `.jar`, sobrescreve estas configurações
e aponta para o MySQL real; a aplicação roda no servidor como serviço próprio,
iniciando junto com a máquina.

Os dois endpoints `/listarUsuario` e `/listarGestor` recebem o id no corpo de um GET
e continuam no código por compatibilidade com o cliente que já os consome. Para uso
novo, prefira `/usuario/{id}/solicitacoes` e `/gestor/{id}/solicitacoes`, que passam
o id no caminho e funcionam de qualquer cliente, incluindo o navegador.
