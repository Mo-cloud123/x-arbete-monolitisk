# x-arbete-monolitisk

Monolitisk e-handelsapplikation byggd med Java 21 och Spring Boot.

## Teknikstack

- Java 21
- Spring Boot (Web, Data JPA, Validation, Actuator)
- PostgreSQL
- Maven
- Docker / Docker Compose

## API

- `POST /users`
- `GET /users/{id}`
- `POST /products`
- `GET /products`
- `POST /orders`
- `GET /orders/{id}`
- `GET /orders/user/{userId}`

## Lokal körning

1. Lägg `DB_PASSWORD` i `.env`.
2. Starta PostgreSQL:
   - `docker compose up -d postgres`
3. Starta appen från IDE eller med Maven wrapper.

## Docker-körning

- Starta allt:
  - `docker compose up -d --build`
- API:
  - `http://localhost:8080`
- Health:
  - `http://localhost:8080/actuator/health`

## Prestandamätning

Appen loggar:
- inkommande requests
- response time per request
- order-skapande
- fel
- periodiska metrics (latency, throughput, CPU, memory)

## Integrationstest

Integrationstester använder Testcontainers + PostgreSQL och verifierar orderflödet:
- skapa user
- skapa product
- skapa order
- hämta order
- verifiera lageruppdatering

## AWS deploy (baseline)

### EC2 (en instans)

1. Starta en Linux EC2-instans.
2. Installera Docker + Docker Compose plugin.
3. Klona repo och sätt `.env` med `DB_PASSWORD`.
4. Kör:
   - `docker compose up -d --build`
5. Öppna port `8080` och `5432` endast vid behov i Security Group.

### Elastic Beanstalk (single instance)

1. Skapa en Docker-baserad Beanstalk-miljö.
2. Bygg/pusha image till ECR.
3. Konfigurera miljövariabler (`DB_PASSWORD` minst).
4. Kör en enkel single-instance miljö för baseline-jämförelse.
