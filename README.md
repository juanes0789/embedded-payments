# Embedded Payments Platform API

Backend API for an embedded payments platform built with Spring Boot using a modular monolith architecture.

## Architecture

Root package: `com.paymentplatform.embeddedpayments`

Modules:
- `auth`
- `merchant`
- `payment`
- `transaction`
- `refund`

Shared modules:
- `shared.security`
- `shared.audit`
- `shared.logging`
- `shared.exception`

Infrastructure modules:
- `infrastructure.config`
- `infrastructure.persistence`
- `infrastructure.cache`
- `infrastructure.webhooks`

### Layer Rules

Each business module follows:
- `api`: REST controllers
- `application`: use cases
- `domain.entity`: domain entities
- `domain.repository`: repository interfaces
- `domain.services`: domain services
- `infrastructure.repository`: JPA implementations

Dependency direction:
- Controllers -> Use cases
- Use cases -> Domain services and domain repository interfaces
- Infrastructure repositories -> Domain repository interfaces

## Local Run

Requirements:
- Java 21+
- Maven Wrapper

```bash
./mvnw spring-boot:run
```

## Deploy en Render

Este proyecto ya queda preparado para Render con:

- `server.port=${PORT:8085}` para respetar el puerto inyectado por la plataforma.
- `render.yaml` con build y start command.
- El build de Render recalcula `JAVA_HOME` desde `javac` para evitar el error del Maven Wrapper.

Variables de entorno requeridas en Render:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=none`

Comandos usados por Render:

```bash
./mvnw clean package -DskipTests
java -jar target/embedded-payments-0.0.1-SNAPSHOT.jar
```

Notas:

- La base de datos puede ser Render Postgres o Neon.
- Antes de la demo, hay que ejecutar `db/001_create_schema.sql` en la base seleccionada.
- El endpoint de salud y Swagger pueden usarse para validar el deploy.

## Tests

```bash
./mvnw test
```

The test profile uses in-memory H2.

## API Endpoints (initial skeleton)

- `POST /api/v1/auth/token`
- `POST /api/v1/merchants`
- `POST /api/v1/payments/intents`
- `POST /api/v1/transactions`
- `POST /api/v1/refunds`
- `POST /api/v1/webhooks`

Swagger UI:
- `/swagger-ui/index.html`

## Git Workflow

Main branches:
- `main`: stable code
- `develop`: integration

Feature branches from `develop`:
- `feature/auth-module`
- `feature/merchant-module`
- `feature/payment-module`
- `feature/transaction-module`
- `feature/refund-module`

Suggested setup:

```bash
git checkout -b develop
git push -u origin develop
```

Example feature flow:

```bash
git checkout develop
git pull
git checkout -b feature/auth-module
# work and commit
git checkout develop
git merge --no-ff feature/auth-module
```

## Commit Convention

- `feat:` new feature
- `fix:` bug fix
- `refactor:` internal improvement
- `docs:` documentation changes
- `chore:` tooling or config

