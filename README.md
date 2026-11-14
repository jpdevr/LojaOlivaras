# Loja Olivaras

Loja acadêmica fictícia de varinhas. O projeto foi preparado para Java 21, Spring Boot, PostgreSQL e futura interface Angular.

## Ambiente local

Requisitos: Docker Desktop com Docker Compose. Copie `.env.example` para `.env` se necessário e rode:

```powershell
docker compose up --build
```

- API: `http://localhost:8080/api/v1`
- Health check: `http://localhost:8080/api/v1/health`
- Swagger (health check inicial): `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

Parar os serviços: `docker compose down`. Para remover também os dados locais do banco: `docker compose down -v`.

## Estado atual

O backend tem a estrutura inicial e a primeira fatia funcional de autenticação: cadastro de cliente, login com e-mail/senha e login com Google ID token. As demais áreas da loja ainda serão feitas por etapas. O Swagger documenta os endpoints atuais em `http://localhost:8080/swagger-ui.html`.

### Cadastro e login

- `POST /api/v1/auth/register`: recebe `profileName`, `email`, `password`, `phone` e `birthDate` (`AAAA-MM-DD`). A pessoa precisa ter **mais de 16 anos**; quem tem exatamente 16 ainda não pode se cadastrar.
- `POST /api/v1/auth/login`: recebe `email` e `password`; devolve JWT Bearer e dados públicos da conta.
- `POST /api/v1/auth/google`: recebe `idToken`. No primeiro acesso, também recebe `profileName`, `phone` e `birthDate`, pois o login básico do Google não fornece esses campos obrigatórios da loja. O Google ID token é verificado no servidor quanto à assinatura, emissor, audiência e e-mail verificado.

Senha local é armazenada com BCrypt. Cadastro público sempre cria `CUSTOMER`; o papel não é aceito no corpo da requisição. Conta Google não recebe senha local e usa a identidade Google vinculada.

## Dependências externas futuras

- Recuperação de senha precisará de SMTP ou provedor de e-mail.
- CEP pode ser consultado via ViaCEP, sem chave de API.
- Fotos de perfil e imagens dos produtos precisam de uma solução de armazenamento; PostgreSQL guardará somente metadados/URLs.
- A conversão Galeão/Real usa uma taxa local ilustrativa definida no `.env`.
- Para habilitar Google, crie um OAuth Client ID no Google Cloud Console, defina `GOOGLE_AUTH_ENABLED=true` e preencha `GOOGLE_CLIENT_ID` no `.env`. O fluxo escolhido valida um ID token enviado pelo cliente, por isso não precisa de client secret no backend. O frontend futuro usará o mesmo Client ID e deverá ter sua origem autorizada no Google Cloud Console.
