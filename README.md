# Rewards API

A Spring Boot REST API that calculates customer reward points for transactions in the current and previous two calendar months.

## Reward rules

Points are calculated **independently for each transaction**, then summed by month and customer:

* The first $50 earns no points.
* Each whole dollar from $50 through $100 earns 1 point.
* Each whole dollar above $100 earns 2 points.
* Cents do not earn partial points.

Examples: `$50.99` earns 0 points, `$75.00` earns 25 points, `$100.00` earns 50 points, and `$120.00` earns 90 points.

The reward period is three calendar months inclusive: the current month and the two preceding months. Each response includes all three months, even when a month has no transactions.

## API

### `GET /rewards`

Returns reward summaries for all customers.

### `GET /rewards/{customerId}`

Returns one customer's monthly and total rewards.

Example response:

```json
{
  "customerId": 1,
  "monthlyRewards": [
    { "month": "2026-08", "points": 0 },
    { "month": "2026-09", "points": 90 },
    { "month": "2026-10", "points": 0 }
  ],
  "totalRewards": 90
}
```

### `GET /transactions`

Returns all transactions, ordered by transaction date, with each transaction's calculated reward points.

### Errors

Errors use Spring's Problem Details JSON format (`type`, `title`, `status`, and `detail`):

* `400 Bad Request` — customer ID is not numeric.
* `404 Not Found` — customer does not exist or the requested path is unknown.
* `405 Method Not Allowed` — the route does not support the requested HTTP method.
* `500 Internal Server Error` — an unexpected application error.

Unexpected errors return a generic detail rather than exposing internal exception information.

## Technology and structure

* Java 25, Spring Boot, Spring Web MVC
* Spring Data JPA / Hibernate
* H2 in-memory database
* Maven

The application is organized into controllers, services, repositories, JPA models, response DTOs, exception handling, and configuration. At startup, it creates three sample customers and four transactions for each, dated relative to the current month. The H2 schema uses `create-drop`, so data is recreated on every application start. Open EntityManager in View is disabled.

## Run, build, and test

Prerequisites: Java 25 or later and Maven.

Start the application:

```powershell
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.

Build and run the full test suite:

```powershell
mvn clean verify
```

The tests cover reward thresholds and per-transaction calculations, monthly aggregation, the three-month window including year boundaries, empty customer histories, REST responses and errors, and seeded-data regression behavior.
