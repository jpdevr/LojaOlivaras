# Magia Olivaras

**Magia Olivaras** é uma loja acadêmica de varinhas. O repositório **LojaOlivaras** reúne a vitrine pública, a área do cliente e o painel administrativo. A moeda é o galeão. Pagamento e entrega são simulados.

## Estrutura

| Pasta | Responsabilidade |
| --- | --- |
| [`frontend/`](frontend/) | Angular 21 e TypeScript. Vitrine, catálogo, carrinho, conta, pedidos, adivinhação e administração. |
| [`backend/`](backend/) | Java 21 e Spring Boot. API REST, autenticação JWT, regras de negócio e simulação dos pedidos. |
| [`backend/src/main/resources/db/migration/`](backend/src/main/resources/db/migration/) | Migrações Flyway e catálogo inicial no PostgreSQL 17. |

## Como rodar

### 1. Instale os pré-requisitos

Você precisa de Git, Docker com Docker Compose e Node.js 24 com npm. O Docker compila e executa o backend com Java 21 e sobe o PostgreSQL. A instalação local de Java, Maven e PostgreSQL é opcional.

### 2. Configure o `.env`

Clone o projeto ou abra a cópia recebida. Na primeira execução, copie o arquivo de exemplo na raiz do repositório.

```powershell
git clone https://github.com/jpdevr/LojaOlivaras.git
cd LojaOlivaras
Copy-Item .env.example .env
```

No Bash, use `cp .env.example .env`. Edite o `.env`, defina `POSTGRES_PASSWORD` e substitua `JWT_SECRET` por uma chave aleatória de pelo menos 32 bytes. Use o comando abaixo para gerar a chave no PowerShell e copie o resultado para `JWT_SECRET=`.

```powershell
[Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
```

| Variável | Exemplo local | Uso |
| --- | --- | --- |
| `POSTGRES_DB` | `olivaras` | Nome do banco. |
| `POSTGRES_USER` | `olivaras` | Usuário do banco. |
| `POSTGRES_PASSWORD` | Defina no `.env` | Senha do PostgreSQL usada pela API. |
| `POSTGRES_PORT` | `5432` | Porta do banco na máquina. Dentro do Docker, a API acessa `db:5432`. |
| `SERVER_PORT` | `8080` | Porta publicada na máquina. O contêiner continua na porta `8080`. Se mudar, atualize `API_BASE_URL` em [`frontend/src/app/core/api.ts`](frontend/src/app/core/api.ts). |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origem do Angular. Aceita uma lista separada por vírgulas. |
| `JWT_SECRET` | Chave aleatória de 32 bytes ou mais | Assinatura e validação dos tokens. |
| `JWT_EXPIRATION_MINUTES` | `30` | Validade do token de login. |

O Git ignora o `.env`. As variáveis de criação do PostgreSQL valem na primeira inicialização do volume. Se você reutilizar um volume existente, use as credenciais desse volume.

### 3. Suba a API e o banco

Execute estes comandos na raiz do projeto.

```powershell
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

O Flyway cria o esquema e carrega o catálogo inicial. Aguarde o backend iniciar e confira a [saúde da API](http://localhost:8080/api/v1/health) ou o [Swagger](http://localhost:8080/swagger-ui.html). No último comando, `Ctrl+C` encerra a exibição dos logs.

### 4. Suba o Angular

Em outro terminal, entre no diretório do frontend para instalar as dependências e iniciar o Angular.

```powershell
cd frontend
npm ci
npm start
```

Abra [Magia Olivaras](http://localhost:4200). Use `localhost` para corresponder à origem padrão do `.env`.

Para gerar a versão otimizada, rode `npm run build` em `frontend/`. O resultado fica em `frontend/dist/frontend/browser/`. Ao publicar como site estático, configure o servidor para encaminhar URLs da SPA, como `/admin`, para `index.html`. Ajuste a URL da API no Angular e a origem no `.env`.

Para parar o Angular, pressione `Ctrl+C` no terminal dele. Na raiz, `docker compose down` para os contêineres e preserva o banco e as imagens enviadas pelo administrador nos respectivos volumes.

### 5. Prepare o administrador no banco local

O cadastro público cria contas `CUSTOMER`. Cadastre uma conta de teste separada na loja. Depois, na raiz, substitua `admin@exemplo.com` pelo e-mail dessa conta no comando abaixo.

```powershell
$dbUser = (docker compose exec -T db printenv POSTGRES_USER).Trim()
$dbName = (docker compose exec -T db printenv POSTGRES_DB).Trim()
$promotionSql = "UPDATE app_user SET role = 'ADMIN', token_version = token_version + 1, updated_at = now() WHERE lower(email) = 'admin@exemplo.com' AND active AND role = 'CUSTOMER' RETURNING email, role;"
$promotionSql | docker compose exec -T db psql -X -v ON_ERROR_STOP=1 -U $dbUser -d $dbName
```

Quando o resultado mostrar `ADMIN`, abra o [painel administrativo](http://localhost:4200/admin) e entre com as credenciais cadastradas. A promoção invalida sessões anteriores da conta.

## Fluxos de uso e regras de negócio

### Vitrine e catálogo

- A página inicial e o catálogo são públicos. Visitantes podem pesquisar, abrir os detalhes de uma varinha e adicionar produtos ao carrinho.
- O catálogo inclui produtos ativos da loja, com preço, estoque e detalhes. A busca considera nome, dono de referência e descrição. Os filtros da interface atuam sobre a página de resultados carregada. O modal usa características em comum para mostrar até três varinhas relacionadas.
- Cada varinha do catálogo inicial tem três imagens locais, entregues com o Angular em `frontend/public/assets/wands/catalogo/`. O modal permite alternar entre elas. No PostgreSQL, `image_url` guarda o caminho da capa e `additional_image_urls` guarda os caminhos das demais imagens.
- A loja tem um tema padrão em dourado e pergaminho e quatro temas de casa. A escolha fica no navegador. O painel administrativo usa o tema padrão, sem névoa.

### Conta, endereços e favoritos

- O cadastro exige nome, e-mail único, telefone, senha e data de nascimento. A idade deve ser superior a 16 anos no fuso de São Paulo. A senha pode ter até 72 bytes em UTF-8 e o hash usa BCrypt. O login por e-mail e senha emite um JWT, que o Angular mantém em memória.
- Perfil, favoritos, adivinhação, checkout e pedidos exigem conta de cliente. Pelo perfil, o cliente edita nome e telefone e troca a senha após confirmar a atual. A troca invalida os tokens anteriores.
- O primeiro endereço vira o padrão. O cliente pode escolher outro e a remoção do padrão promove um dos endereços restantes. O botão de CEP consulta o ViaCEP diretamente pelo navegador. O formulário também aceita preenchimento manual.
- Favoritar o mesmo produto novamente mantém um único registro. Um produto esgotado pode continuar entre os favoritos, com a disponibilidade atual indicada.
- A exclusão da conta pela API exige a senha atual. Ela desativa e anonimiza a conta e os dados de entrega dos pedidos, remove endereços, carrinho e favoritos e retira a varinha pessoal ativa. O histórico de compra fica preservado no banco.

### Carrinho, checkout e pedidos

1. Adicione produtos ao carrinho. O carrinho de visitante fica no navegador. Ao entrar como cliente, a API transfere as unidades disponíveis para a conta. A transferência tem um identificador para aceitar reenvios sem duplicar itens.
2. Selecione os itens que deseja comprar. O limite é de 99 unidades por produto, respeitando o estoque. A disponibilidade é verificada novamente na confirmação.
3. No checkout, informe o CPF e selecione um endereço próprio e um meio de pagamento. O formulário e a API validam os dígitos verificadores do CPF. O servidor guarda os 11 dígitos no pedido, sem retorná-los na API, e apaga esse dado se a conta for excluída. Durante a confirmação, o servidor bloqueia o estoque, grava os preços e o endereço usados e retira do carrinho somente os itens comprados.
4. Acompanhe o pagamento e a entrega em **Meus pedidos**. O pedido começa como `CONFIRMED` com pagamento `PENDING`. Após o prazo, o pagamento passa a `SIMULATED_APPROVED` e o pedido a `PROCESSING`. Um dia após a aprovação, o pedido vira `DELIVERED`. O backend verifica as transições a cada cinco segundos por padrão.

| Meio | Prazo de aprovação | Resultado na interface |
| --- | --- | --- |
| `PIX` | 5 minutos | QR code enquanto o pagamento está pendente. |
| `BOLETO` | 15 minutos | Boleto para baixar em PDF. |
| `CREDIT_CARD` | 2 minutos | Acompanhamento sem coleta de dados de cartão. |

O QR code e o boleto são fictícios. As transições ilustram o processo de compra acadêmico.

### Adivinhação

- Entre como cliente e abra **Descubra sua varinha** na página inicial ou **Minha varinha** pelo menu da conta.
- Dez respostas determinam madeira, núcleo, comprimento e flexibilidade. A combinação de madeira e núcleo escolhe uma de vinte artes. O cliente é o dono da varinha, com preço de sete galeões e estoque inicial de uma unidade.
- Cada conta tem uma varinha pessoal ativa por vez. Ela pode ir para o carrinho do dono e fica fora do catálogo público. Repetir a revelação retorna a varinha ativa. Destruí-la retira a unidade do carrinho, preserva referências de pedidos anteriores e libera outro teste.

### Administração

- **Vendas** mostra receita aprovada, varinhas vendidas, quantidade de pedidos, receita diária e pedidos recentes. A interface oferece 7, 30 e 90 dias. Receita e unidades usam a data de aprovação, pedidos usam a data de criação e o período segue o fuso de São Paulo.
- **Estoque** permite pesquisar por nome, cadastrar varinhas e registrar entradas e retiradas. O cadastro publica a varinha na vitrine. Cada ajuste é serializado por produto, e a API rejeita retirada superior ao saldo. Varinhas pessoais são gerenciadas pelo cliente.
- No cadastro de produto, selecione uma imagem de capa e, se quiser, mais duas fotos. O backend grava os arquivos no volume Docker `wand_images` e registra seus caminhos no PostgreSQL. Ao rodar a API diretamente, as fotos ficam em `./data/wand-images` a partir do diretório de execução. Use `WAND_IMAGE_DIRECTORY` para escolher outro diretório.

## Endpoints

Os caminhos são relativos a `http://localhost:8080`. Para testar rotas protegidas no [Swagger](http://localhost:8080/swagger-ui.html), execute o login e informe apenas o `accessToken` em **Authorize**. Em chamadas HTTP diretas, envie `Authorization: Bearer <accessToken>`.

- Autenticação e catálogo são públicos. Perfil, endereços, wishlist, carrinho, adivinhação e pedidos exigem o papel `CUSTOMER`. As rotas administrativas exigem `ADMIN`.
- IDs entre chaves são UUIDs. Páginas começam em zero. Catálogo e histórico retornam registros em `content`. Estoque administrativo retorna `items`, `totalPages` e `totalItems`.
- Erros comuns incluem `400` por entrada inválida, `401` por autenticação ausente ou inválida, `403` por papel insuficiente, `404` por recurso não encontrado e `409` por conflito. Erros de validação podem informar `fieldErrors`.

### Diagnóstico e documentação

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` | `/api/v1/health` | Identifica a API e retorna o estado básico do serviço. |
| `GET` | `/actuator/health` | Retorna a saúde da aplicação, considerando o banco de dados. |
| `GET` | `/swagger-ui.html` | Entrada do Swagger UI. |
| `GET` | `/swagger-ui/index.html` | Interface do Swagger UI. |
| `GET` | `/v3/api-docs` | Especificação OpenAPI em JSON. |
| `GET` | `/v3/api-docs/swagger-config` | Configuração utilizada pelo Swagger UI. |
| `GET` | `/v3/api-docs.yaml` | Especificação OpenAPI em YAML, acessível com JWT válido. |

### Autenticação e catálogo

| Método | Rota | Entrada e resultado |
| --- | --- | --- |
| `POST` | `/lojaolivaras/api/auth/register` | `profileName`, `email`, `password`, `phone` e `birthDate` em `AAAA-MM-DD`. Cria a conta e retorna `201`. |
| `POST` | `/lojaolivaras/api/auth/login` | `email` e `password`. Retorna `accessToken`, `tokenType`, `expiresIn` em segundos e `user`. |
| `GET` | `/lojaolivaras/api/catalogo/list-item` | Compras e consultas públicas usam o catálogo de varinhas ativas. `page=0`, `size=20` por padrão. `size` aceita de 1 a 100. |
| `GET` | `/lojaolivaras/api/catalogo/search` | Busca por `q`, `wood` e/ou `core`, com a mesma paginação. |
| `GET` | `/lojaolivaras/api/catalogo/item-detail/{id}` | Detalhes e estoque de uma varinha pública. `imageUrls` lista as imagens na ordem de exibição. |
| `GET` | `/lojaolivaras/api/catalogo/item-detail/{id}/related` | Até três varinhas relacionadas do catálogo. |
| `GET` | `/lojaolivaras/api/catalogo/imagens/{id}.{extension}` | Exibe um arquivo local cadastrado pelo administrador. A extensão é `jpg` ou `png`. |

### Perfil e endereços

| Método | Rota | Entrada e resultado |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/profile` | Dados da própria conta. |
| `POST` | `/lojaolivaras/api/profile/edit` | Atualiza `profileName`, `phone` ou ambos. |
| `POST` | `/lojaolivaras/api/profile/change-password` | `currentPassword` e `newPassword`. Retorna `204`. |
| `POST` | `/lojaolivaras/api/profile/deleteaccount` | `currentPassword`. Desativa e anonimiza a conta. Retorna `204`. Disponível por API. |
| `GET` | `/lojaolivaras/api/profile/address` | Lista os endereços próprios. |
| `POST` | `/lojaolivaras/api/profile/address/add` | Cria endereço e retorna `201`. |
| `POST` | `/lojaolivaras/api/profile/address/edit/{addressId}` | Substitui os dados de um endereço próprio. |
| `POST` | `/lojaolivaras/api/profile/address/delete/{addressId}` | Remove endereço e retorna `204`. |
| `POST` | `/lojaolivaras/api/profile/address/default/{addressId}` | Define o endereço como padrão da conta. |

Para criar ou editar endereço, envie `recipient`, `zipCode`, `street`, `number`, `district`, `city` e `state`. O corpo também aceita `label`, `complement`, `reference` e `defaultAddress`. O CEP aceita oito dígitos com hífen opcional e o estado recebe a sigla de duas letras.

### Wishlist e adivinhação

| Método | Rota | Entrada e resultado |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/wishlist/list` | Lista os favoritos da conta. |
| `POST` | `/lojaolivaras/api/wishlist/add` | `itemId` do catálogo. Salva um favorito. |
| `POST` | `/lojaolivaras/api/wishlist/remove` | `itemId` do catálogo. Remove o favorito. |
| `POST` | `/lojaolivaras/api/wishlist/clear` | Limpa todos os favoritos. Disponível por API. |
| `GET` | `/lojaolivaras/api/profile/wand-divination/questions` | Dez perguntas com quatro alternativas por pergunta. |
| `GET` | `/lojaolivaras/api/profile/wand-divination` | Varinha pessoal atual ou `204` se ainda não houver uma. |
| `POST` | `/lojaolivaras/api/profile/wand-divination` | `answers`, lista de dez índices de `0` a `3` na ordem das perguntas. Retorna a varinha pessoal. |
| `DELETE` | `/lojaolivaras/api/profile/wand-divination/{wandId}` | Retira a própria varinha ativa e retorna `204`. |

### Carrinho

| Método | Rota | Entrada e resultado |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/cart/list` | Itens, disponibilidade e totais da conta. |
| `GET` | `/lojaolivaras/api/cart/total` | `totalGalleons` e `totalItems`. O Angular usa o total de unidades na navegação. |
| `POST` | `/lojaolivaras/api/cart/add` | `itemId` e `quantity`. Adiciona ou soma unidades. |
| `POST` | `/lojaolivaras/api/cart/merge` | `transferId` e `items`, lista com pares `itemId` e `quantity`. Retorna `skippedUnits`. |
| `POST` | `/lojaolivaras/api/cart/update-quantity/{itemId}` | `quantity`. Substitui a quantidade de um produto no carrinho. |
| `POST` | `/lojaolivaras/api/cart/remove` | `itemId`. Retira o produto do carrinho. |
| `POST` | `/lojaolivaras/api/cart/clear` | Esvazia o carrinho. |

### Checkout e pedidos

| Método | Rota | Entrada e resultado |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/checkout/addresses` | Lista endereços próprios para entrega usando a consulta do perfil. |
| `POST` | `/lojaolivaras/api/checkout/confirm` | `addressId`, `paymentMethod`, `cpf` com ou sem pontuação e `itemIds` opcional para seleção parcial. Sem `itemIds`, tenta comprar o carrinho inteiro. Cria o pedido e retorna `201`. |
| `GET` | `/lojaolivaras/api/checkout/order/{id}` | Detalhes de pedido próprio, incluindo itens e endereço. |
| `GET` | `/lojaolivaras/api/checkout/payment-status/{orderId}` | Estado, prazo e dados do pagamento. Inclui QR code em data URI para PIX pendente. |
| `GET` | `/lojaolivaras/api/checkout/order/{id}/boleto` | PDF de um pedido que usa boleto. |
| `GET` | `/lojaolivaras/api/profile/orders` | Histórico em ordem decrescente de criação. `page=0` e `size=20` por padrão, com `size` de 1 a 50. |
| `GET` | `/lojaolivaras/api/profile/orders/` | Alias do histórico com barra final. |

### Administração

| Método | Rota | Entrada e resultado |
| --- | --- | --- |
| `GET` | `/lojaolivaras/api/admin/sales` | Indicadores e pedidos recentes. `days=30` por padrão e aceita de 1 a 90. |
| `GET` | `/lojaolivaras/api/admin/inventory` | Estoque em páginas de 20. Aceita `search` para buscar por nome e `page`. |
| `POST` | `/lojaolivaras/api/admin/inventory` | Cadastra uma varinha por `multipart/form-data` e retorna `201`. |
| `PATCH` | `/lojaolivaras/api/admin/inventory/{id}/stock` | `change`, inteiro positivo para entrada ou negativo para retirada. Retorna o saldo atualizado. |

A parte `product` deve ter tipo `application/json` e incluir `name`, `description`, `priceGalleons` e `stock`. Os campos opcionais são `wood`, `core`, `lengthInches`, `flexibility` e `maker`. Envie cada arquivo na parte `images` e coloque a capa primeiro. A API aceita de um a três arquivos PNG ou JPEG, com até 5 MB e 4096 × 4096 pixels por arquivo.

```powershell
curl.exe "http://localhost:8080/lojaolivaras/api/admin/inventory" `
  -H "Authorization: Bearer $adminToken" `
  -F "product=@produto.json;type=application/json" `
  -F "images=@capa.png" `
  -F "images=@detalhe.jpg"
```

## Licença

O código usa a [licença MIT](LICENSE). As licenças das fontes ficam em [`frontend/public/assets/fonts/`](frontend/public/assets/fonts/).
