# Rewards API

A Spring Boot REST API that calculates customer reward points for transactions in a three-calendar-month period.

## Technology

* Java 25
* Spring Boot and Spring Web MVC
* Spring Data JPA / Hibernate
* H2 in-memory database
* Maven

## Run and Build

Prerequisite: Java 25 or later. The Maven Wrapper downloads and runs the configured Maven version, so a separate Maven installation is not required.

Start the application:

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

Build and run the full test suite:

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
```

macOS/Linux:

```bash
./mvnw clean verify
```

## Reward Rules

Points are calculated **independently for each transaction**, then summed by month and customer:

* The first $50 earns no points.
* Each whole dollar from $50 through $100 earns 1 point.
* Each whole dollar above $100 earns 2 points.
* Cents do not earn partial points.

Examples: `$50.99` earns 0 points, `$75.00` earns 25 points, `$100.00` earns 50 points, and `$120.00` earns 90 points.

The reward period is three calendar months inclusive: the current month and the two preceding months. Each reward response includes all three months, even when a month has no transactions.

## API

### `GET /rewards`

Returns an array of customer reward objects in the same format as `GET /rewards/{customerId}`.

```bash
curl http://localhost:8080/rewards
```

Example response:

```json
[
  {
    "customerId": 1,
    "monthlyRewards": [
      { "month": "2026-08", "points": 0 },
      { "month": "2026-09", "points": 90 },
      { "month": "2026-10", "points": 0 }
    ],
    "totalRewards": 90
  }
]
```

### `GET /rewards/{customerId}`

Returns one customer's monthly and total rewards.

```bash
curl http://localhost:8080/rewards/1
```

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

Returns all transactions, ordered by transaction date, with reward points calculated for each transaction.

```bash
curl http://localhost:8080/transactions
```

Example transaction:

```json
{ "id": 4, "customerId": 1, "amount": 49.97, "transactionDate": "2026-10-09", "rewardPoints": 0 }
```

### Sample customers and expected rewards

On a fresh database, the sample customers receive IDs 1 through 3. The table shows their expected points for the two preceding months and current month; the month labels move with the current date.

| Customer | Month −2 | Month −1 | Current | Total |
|---|---:|---:|---:|---:|
| 1 Anna Smith | 0 | 90 | 0 | 90 |
| 2 Bob Williams | 0 | 13 | 160 | 173 |
| 3 Chris Johnson | 47 | 50 | 50 | 147 |

## Errors

Errors use Spring's Problem Details JSON format (`type`, `title`, `status`, and `detail`):

* `400 Bad Request` — customer ID is not numeric.
* `404 Not Found` — customer does not exist or the requested path is unknown.
* `405 Method Not Allowed` — the route does not support the requested HTTP method.
* `500 Internal Server Error` — an unexpected application error.

Unexpected errors return a generic detail rather than exposing internal exception information.

## Application Structure

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

* **Controller** – Exposes the rewards and transactions HTTP endpoints.
* **Service** – Calculates per-transaction reward points, filters the three-month period, and aggregates monthly and customer totals.
* **Repository** – Provides customer and transaction persistence through Spring Data JPA.
* **Model** – Defines the JPA customer and transaction entities.
* **DTO** – Defines the customer reward, monthly reward, and transaction response records.
* **Exception** – Maps application and HTTP errors to Spring Problem Details responses.
* **Config** – Provides the application clock and loads sample customers and transactions at startup.

At startup, the application creates three sample customers and four transactions for each, dated relative to the current month. The in-memory database is recreated on every start.

## Testing

The application includes tests at multiple levels:

* **Calculator unit tests** – Verify reward thresholds and whole-dollar calculation.
* **Service unit tests** – Verify per-transaction calculation, monthly aggregation, customer validation, empty histories, and the three-month window across year boundaries.
* **Controller tests** – Use standalone MockMvc with mocked services to verify endpoint responses, HTTP status codes, and Problem Details error bodies.
* **Spring Boot integration test** – Starts the application context and checks reward totals and transaction responses against the seeded data.
