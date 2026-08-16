# authorization-server

Microservice responsible for issuing and managing OAuth2 / OpenID Connect tokens for the [AlgaShop](https://github.com/jeanmalvessi/ems-algashop-meta) platform.

Built with **Spring Authorization Server**, acting as the platform's identity provider: every other microservice validates access tokens against it as an OAuth2 resource server, and `ordering` additionally uses it as an OAuth2 client.

## Responsibilities

- User account management (registration, updates, activation, anonymization) for `MANAGER`, `OPERATOR`, and `CUSTOMER` user types
- Email verification and password reset via time-limited, hashed verification tokens, delivered asynchronously by email
- OAuth2 Authorization Code (with PKCE), Client Credentials, and Refresh Token grants, plus OpenID Connect (login, consent, UserInfo, RP-Initiated Logout)
- Issuing self-contained JWT access tokens signed with an RSA key and exposing a JWKS endpoint for downstream token validation
- Role- and client-scoped authorization: restricting which OAuth2 scopes and clients each user type can be granted
- Login, consent, logout, and password-reset pages (Thymeleaf)

## Architecture

- **Domain Layer:** `AuthUser` aggregate (password hashing, verification tokens, activation/anonymization), `AuthUserType` (`MANAGER`, `OPERATOR`, `CUSTOMER`)
- **Application Layer:** `AuthUserManagementApplicationService`, `PasswordManagementApplicationService`, `AuthUserQueryService`, `SecurityChecks` (method-security authorization policy)
- **Infrastructure Layer:** Spring Authorization Server configuration (`AuthorizationServerSecurityConfig`), JDBC-backed `OAuth2AuthorizationService` / `OAuth2AuthorizationConsentService`, RSA/JWKS signing key (`JwkSourceConfig`), role/client scope resolution (`ScopePolicyService`), CORS/CSP/cookie hardening, asynchronous mail sending, Spring Session JDBC
- **Presentation Layer:** REST API for user management (`/api/v1/users`) plus MVC controllers for login, consent, logout, and password pages

## Tech Stack

- **Java 25**, Spring Boot 4.0.6
- **Spring Authorization Server** (OAuth2 Authorization Server + OpenID Connect provider)
- **Spring Security** (method security, CORS, CSP, cookie hardening)
- **Spring Data JPA** + PostgreSQL 17 (persistence)
- **Flyway** (database migrations)
- **Spring Session JDBC** (server-side session store, backed by PostgreSQL)
- **Spring Boot Actuator** (monitoring and health checks)
- **Spring Cloud AWS Secrets Manager + Parameter Store** (externalized config: datasource credentials, RSA signing key, OAuth2 client secrets/redirect URIs, mocked via LocalStack)
- **RSA-signed JWT** access tokens (self-contained) with a JWKS endpoint for verification
- **Thymeleaf** (login, consent, logout, and password pages)
- **Spring Boot Starter Mail** (asynchronous activation and password-reset emails)
- **java-uuid-generator** (identifiers)
- **Commons Lang3**
- **Lombok**

## OAuth2 Clients

| Client ID | Grant Type(s) | Authentication | Notes |
|-----------|---------------|-----------------|-------|
| `algashop-admin-web` | `authorization_code` (PKCE) | none (public client) | Back-office web app for `MANAGER`/`OPERATOR` users |
| `algashop-ecommerce-web` | `authorization_code`, `refresh_token` | `client_secret_basic` | Storefront web app for `CUSTOMER` users |
| `algashop-ecommerce-m2m` | `client_credentials` | `client_secret_basic` | Storefront backend calling downstream services |
| `algashop-ordering-service` | `client_credentials` | `client_secret_basic` | `ordering` calling `product-catalog` |
| `algashop-test` | `client_credentials` | `client_secret_basic` | Local/manual testing with the full scope set |

## API

Base path: `/api/v1`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/users` | Register a user (`MANAGER`, `OPERATOR`, or `CUSTOMER`) |
| GET | `/users` | List/filter users with pagination |
| GET | `/users/{userId}` | Get user by ID |
| PUT | `/users/{userId}` | Update user |
| DELETE | `/users/{userId}` | Delete (anonymize) user |
| GET | `/users/me` | Get the authenticated user's own profile |
| PUT | `/users/me` | Update the authenticated user's own profile |
| DELETE | `/users/me` | Delete (anonymize) the authenticated user's own profile |
| POST | `/users/me/password-change` | Request a password-change email for the authenticated user |

Plus the standard OAuth2/OIDC endpoints (`/oauth2/authorize`, `/oauth2/token`, `/oauth2/jwks`, `/oauth2/revoke`, `/oauth2/introspect`, `/.well-known/openid-configuration`, `/userinfo`, `/connect/logout`) and the browser-facing `/login`, `/logout`, `/oauth2/consent`, `/change-password`, and `/forgot-password` pages.

## Running

```bash
./gradlew bootRun
```

Default port: **8081** (development profile), host-bound to `auth.algashop.local` — add the aliases from the meta-repo's `etc/hostnames` to your hosts file for OAuth2 redirect flows to work correctly.

Database: PostgreSQL `authorization_server` database on `localhost:5432`
