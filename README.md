# Loja Olivaras

Loja acadêmica fictícia de varinhas. O projeto foi preparado para Java 21, Spring Boot, PostgreSQL e futura interface Angular.

## Ambiente local

Requisitos: Docker Desktop com Docker Compose. Copie `.env.example` para `.env` se necessário e rode:

```powershell
docker compose up --build
```

- API: `http://localhost:8080/lojaolivaras/api`
- Health check: `http://localhost:8080/api/v1/health`
- Swagger: `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

Parar os serviços: `docker compose down`. Para remover também os dados locais do banco: `docker compose down -v`.

## Frontend Angular

O frontend fica em `frontend/` e usa Angular com TypeScript. A tela atual contém login e cadastro com HTML sem folha de estilos; os formulários chamam as rotas de auth do backend.

Requisitos: Node.js 24 e npm. Inicie o backend e, em outro terminal, rode:

```powershell
cd frontend
npm start
```

Abra `http://localhost:4200`. O token retornado pelo login fica apenas em memória enquanto a aplicação estiver aberta.

## Estado atual

O backend tem autenticação, perfil/endereço, catálogo, carrinho, checkout/pedidos e wishlist. O Swagger documenta os endpoints em `http://localhost:8080/swagger-ui.html`.

### Documentação e diagnóstico

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` | `/swagger-ui.html` | Abre a interface do Swagger. |
| `GET` | `/v3/api-docs` | Retorna a especificação OpenAPI em JSON. |
| `GET` | `/v3/api-docs/swagger-config` | Retorna a configuração usada pelo Swagger UI. |
| `GET` | `/api/v1/health` | Health check simples da API. |
| `GET` | `/actuator/health` | Diagnóstico de saúde do Spring Boot. |
| `GET` | `/actuator/info` | Informações da aplicação. |

### Cadastro e login

- `POST /lojaolivaras/api/auth/register`: recebe `profileName`, `email`, `password`, `phone` e `birthDate` (`AAAA-MM-DD`). A pessoa precisa ter **mais de 16 anos**; quem tem exatamente 16 ainda não pode se cadastrar.
- `POST /lojaolivaras/api/auth/login`: recebe `email` e `password`; devolve JWT Bearer e dados públicos da conta.
- `POST /lojaolivaras/api/auth/google`: recebe `idToken`. No primeiro acesso, também recebe `profileName`, `phone` e `birthDate`, pois o login básico do Google não fornece esses campos obrigatórios da loja. O Google ID token é verificado no servidor quanto à assinatura, emissor, audiência e e-mail verificado.

Senha local é armazenada com BCrypt. Cadastro público sempre cria `CUSTOMER`; o papel não é aceito no corpo da requisição. Conta Google não recebe senha local e usa a identidade Google vinculada.

### Perfil e endereços

Todas as rotas abaixo exigem `Authorization: Bearer <token>` obtido no login. O servidor identifica a conta pelo `sub` do JWT; nunca envie ou aceite um `userId` do cliente para escolher o perfil.

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/profile` | Consulta o próprio perfil (rota adicional para visualização). |
| `POST` | `/lojaolivaras/api/profile/edit` | Atualiza `profileName` e/ou `phone`. E-mail e data de nascimento não são editáveis aqui. |
| `POST` | `/lojaolivaras/api/profile/change-password` | Altera a senha local; exige senha atual e nova senha (10 a 72 caracteres). |
| `POST` | `/lojaolivaras/api/profile/deleteaccount` | Encerra a conta após reautenticação: senha atual ou ID token Google recente. |
| `GET` | `/lojaolivaras/api/profile/address` | Lista os próprios endereços. |
| `POST` | `/lojaolivaras/api/profile/address/add` | Cadastra endereço; o primeiro vira padrão automaticamente. |
| `POST` | `/lojaolivaras/api/profile/address/edit/{id}` | Substitui um endereço da própria conta. |
| `POST` | `/lojaolivaras/api/profile/address/delete/{id}` | Remove endereço próprio; se era padrão, promove outro. |
| `GET` | `/lojaolivaras/api/profile/orders/` | Lista os próprios pedidos, paginados; também aceita sem a barra final. |
| `GET` | `/lojaolivaras/api/profile/orders/{id}` | Consulta um pedido próprio. |

### Catálogo

As consultas do catálogo são públicas. Os itens são páginas ordenadas pelo nome; `page` começa em zero e `size` aceita de 1 a 100. A resposta exibe disponibilidade, não a quantidade exata em estoque.

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/catalogo/list-item` | Lista itens ativos. Parâmetros opcionais: `page`, `size`. |
| `GET` | `/lojaolivaras/api/catalogo/search` | Busca por `q`, `wood` e/ou `core`; também aceita `page` e `size`. |
| `GET` | `/lojaolivaras/api/catalogo/item-detail/{id}` | Detalha item pelo UUID. |

O seed inicial contém 20 varinhas com informações documentadas, incluindo uma baseada em detalhes de produção de filme. Características sem confirmação ficam vazias, preços e estoques são fictícios para a atividade e as imagens ficam sem URL até haver material autorizado. Cada item guarda a referência e a categoria da fonte.

### Carrinho

As rotas exigem JWT Bearer de uma conta `CUSTOMER`. Cada conta possui um carrinho persistido. O total é recalculado pelo preço atual do catálogo; quantidade por produto aceita de 1 a 99. O carrinho não reserva estoque: a disponibilidade é conferida novamente na finalização.

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/cart/list` | Lista itens, disponibilidade e total do próprio carrinho. |
| `POST` | `/lojaolivaras/api/cart/add` | Adiciona ou soma quantidade. Corpo: `{"itemId":"<UUID do produto>","quantity":1}`. |
| `POST` | `/lojaolivaras/api/cart/remove` | Remove produto. Corpo: `{"itemId":"<UUID do produto>"}`. |
| `POST` | `/lojaolivaras/api/cart/update-quantity/{itemId}` | Define nova quantidade. Corpo: `{"quantity":2}`. |
| `POST` | `/lojaolivaras/api/cart/clear` | Esvazia o carrinho. |
| `GET` | `/lojaolivaras/api/cart/total` | Consulta total e quantidade de unidades. |

### Checkout e pedidos

O checkout exige um endereço cadastrado na própria conta e um carrinho não vazio. A confirmação grava preço, características do produto e endereço como fotografia do momento do pedido, reserva o estoque atomicamente e esvazia o carrinho. O pedido começa como `CONFIRMED` e o pagamento como `PENDING`. A simulação aprova automaticamente o pagamento após o prazo do método; o pedido passa a `PROCESSING` e é marcado `DELIVERED` um dia depois. Um agendador no backend aplica essas mudanças mesmo se o cliente sair da página.

| Método | Rota | Uso |
| --- | --- | --- |
| `POST` | `/lojaolivaras/api/checkout/confirm` | Confirma o pedido e inicia o pagamento simulado. Corpo: `{"addressId":"<UUID do endereço>","paymentMethod":"PIX"}`. Métodos: `PIX` (5 min), `BOLETO` (15 min) e `CREDIT_CARD` (2 min). |
| `GET` | `/lojaolivaras/api/checkout/addresses` | Lista endereços próprios para entrega. |
| `GET` | `/lojaolivaras/api/checkout/order/{id}` | Consulta um pedido próprio. |
| `GET` | `/lojaolivaras/api/checkout/payment-status/{orderId}` | Consulta estado, prazo restante e instruções simuladas; para PIX também retorna QR code PNG em data URI. |
| `GET` | `/lojaolivaras/api/checkout/order/{id}/boleto` | Baixa PDF fictício com os dados do pedido, apenas para pedidos com método boleto. |
| `GET` | `/lojaolivaras/api/profile/orders/` | Lista o histórico e os status atuais dos pedidos próprios. |
| `GET` | `/lojaolivaras/api/profile/orders/{id}` | Consulta um pedido próprio pelo histórico. |

O QR code contém um identificador de demonstração e não inicia uma transferência PIX real. O boleto PDF também é fictício e não pode ser pago em banco. O checkout não pede nem armazena dados de cartão. `SIMULATED_APPROVED` significa apenas que o prazo da simulação terminou; nenhuma cobrança real foi feita.

Após exclusão da conta, o carrinho é removido e dados de endereço copiados para pedidos são anonimizados.

### Wishlist

As rotas exigem JWT Bearer de uma conta `CUSTOMER`. A wishlist pertence ao usuário autenticado; `itemId` é o UUID do produto do catálogo. Adicionar o mesmo produto novamente não cria duplicata. Itens fora de estoque continuam salvos, mas são marcados como indisponíveis.

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/wishlist/list` | Lista itens salvos e disponibilidade atual. |
| `POST` | `/lojaolivaras/api/wishlist/add` | Salva produto. Corpo: `{"itemId":"<UUID do produto>"}`. |
| `POST` | `/lojaolivaras/api/wishlist/remove` | Remove produto. Corpo: `{"itemId":"<UUID do produto>"}`. |
| `POST` | `/lojaolivaras/api/wishlist/clear` | Limpa a própria wishlist. |

A wishlist também é removida quando a conta é excluída.

Exemplos de corpos:

```json
{"profileName":"Luna","phone":"+5511999999999"}
```

```json
{"currentPassword":"senha-atual","newPassword":"uma-senha-nova-segura"}
```

As respostas não incluem senha, hash, identidade Google ou token. Senhas locais são armazenadas como hash BCrypt. Alterar a senha invalida todos os JWTs já emitidos. Excluir conta exige reautenticação, apaga os endereços e anonimiza os dados pessoais básicos; o registro inativo permanece para integridade futura. Conta Google não tem senha local para alterar. A edição de e-mail e a recuperação de acesso ficam para uma etapa com verificação de e-mail.

## Dependências externas futuras

- Recuperação de senha precisará de SMTP ou provedor de e-mail.
- CEP pode ser consultado via ViaCEP, sem chave de API.
- Fotos de perfil e imagens dos produtos precisam de uma solução de armazenamento; PostgreSQL guardará somente metadados/URLs.
- A conversão Galeão/Real usa uma taxa local ilustrativa definida no `.env`.
- Para habilitar Google, crie um OAuth Client ID no Google Cloud Console, defina `GOOGLE_AUTH_ENABLED=true` e preencha `GOOGLE_CLIENT_ID` no `.env`. O fluxo escolhido valida um ID token enviado pelo cliente, por isso não precisa de client secret no backend. O frontend futuro usará o mesmo Client ID e deverá ter sua origem autorizada no Google Cloud Console.
