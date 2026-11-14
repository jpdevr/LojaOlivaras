# Loja Olivaras

Loja acadêmica fictícia de varinhas. O projeto foi preparado para Java 21, Spring Boot, PostgreSQL e futura interface Angular.

## Ambiente local

Requisitos: Docker Desktop com Docker Compose. Copie `.env.example` para `.env` se necessário e rode:

```powershell
docker compose up --build
```

- API: `http://localhost:8080/lojaolivaras/api`
- Health check: `http://localhost:8080/api/v1/health`
- Swagger (health check inicial): `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

Parar os serviços: `docker compose down`. Para remover também os dados locais do banco: `docker compose down -v`.

## Estado atual

O backend tem autenticação e o módulo de perfil/endereço. O Swagger documenta os endpoints em `http://localhost:8080/swagger-ui.html`.

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
