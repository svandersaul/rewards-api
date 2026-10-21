# Rewards API

A Spring Boot REST API that calculates customer reward points based on transaction amounts over a three-month period.

## Overview

Customers earn reward points on each transaction according to the following rules:

* No points are awarded for the first $50 of a transaction.
* Customers earn 1 point for each whole dollar between $50 and $100.
* Customers earn 2 points for each whole dollar above $100.
* Reward calculations are performed **per transaction**, not on the customer's total spending for a month.
* Fractional dollar amounts do not earn partial points.

For example:

* `$50.00` → 0 points
* `$75.00` → 25 points
* `$100.00` → 50 points
* `$120.00` → 90 points

## Technology

* Java 25
* Spring Boot
* Spring Web MVC
* Spring Data JPA / Hibernate
* H2 in-memory database
* Maven

## Application Structure

The application follows a simple layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

* **Controller** – Handles HTTP requests and responses.
* **Service** – Contains the reward calculation and aggregation logic.
* **Repository** – Provides database access through Spring Data JPA.
* **Model** – Represents customers and transactions.
* **DTO** – Defines the data returned by the API.
* **Exception** – Contains custom exceptions and global error handling.
* **Config** – Loads sample customer and transaction data when the application starts.

## API

### Get Customer Rewards

```http
GET /rewards/{customerId}
```

Returns the customer's reward points grouped by month, along with the total reward points.

Example:

```http
GET /rewards/1
```

Example response:

```json
{
  "customerId": 1,
  "monthlyRewards": {
    "2026-06": 0,
    "2026-07": 90,
    "2026-08": 0
  },
  "totalRewards": 90
}
```

### Error Responses

The API returns appropriate HTTP status codes for common errors:

* `400 Bad Request` – Invalid customer ID, such as `/rewards/q`
* `404 Not Found` – Customer does not exist
* `500 Internal Server Error` – Unexpected application error

Errors are returned as JSON responses rather than exposing internal exception details.

## Sample Data

The application uses an H2 in-memory database and loads sample customers and transactions when it starts.

The database is configured with `create-drop`, so the schema and sample data are recreated each time the application starts.

## Running the Application

### Prerequisites

* Java 25 or later
* Maven

### Start the application

On Windows:

```powershell
mvn spring-boot:run
```

On macOS/Linux:

```bash
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

### Build and test

```bash
mvn clean verify
```

This compiles the application and runs the automated tests.

## Testing

The application includes tests at multiple levels:

* **Service unit tests** – Verify reward calculations, transaction aggregation, customer validation, and monthly totals.
* **Controller unit tests** – Verify controller behavior and interaction with the service.
* **HTTP-layer integration tests** – Verify REST endpoints, HTTP status codes, JSON responses, and error handling.

The tests cover reward thresholds, multiple transactions within a month, transactions across multiple months, customers with no transactions, nonexistent customers, and invalid customer IDs.

## Assumptions

* Reward points are awarded for whole-dollar amounts only; fractional dollar amounts do not earn partial points.
* Reward thresholds are applied independently to each transaction.
* The sample data contains transactions from June through August 2026.
* The H2 database is intended for demonstration and development purposes and is not intended as a production database.