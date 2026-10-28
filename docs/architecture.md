# Architecture and data flow

## Request lifecycle

1. **Postman / HTTP client** sends a request to the local Spring Boot server at `localhost:8080`.
2. **`PizzaApiController`** maps the route to the appropriate operation.
3. **Request validation** checks required fields and accepted catalog values. The application also validates duplicate toppings before storing the order.
4. **`PizzaOperations`** abstracts the storage implementation. The `test` profile uses `PizzaService` in memory, while the ordinary server profile uses `JdbcPizzaService` backed by PostgreSQL.
5. **`JdbcPizzaService`** calls PostgreSQL via `JdbcTemplate`; toppings are stored as JSONB in `orders`.
6. **`ApiExceptionHandler`** converts recognized client errors into structured HTTP responses.

## Database

`sql/schema.sql` creates `public.orders` with an identity primary key, pizza options, JSONB toppings, extra topping charge, and timestamp.

The schema is applied **manually** with pgAdmin. The project does not yet include Flyway/Liquibase migrations.

## Read/write operations

- `POST /api/v1/orders` validates and saves the order, then returns `201 Created` and its identifier.
- `GET /api/v1/orders` reads persisted records.
- `GET /api/v1/statistics` computes statistics from the loaded orders in Java. These are **not database-side aggregate queries** in the current API.

## Security and limitations

The script passes DB credentials through a PowerShell environment variable at runtime. Credentials should not be committed. A dedicated least-privilege DB role and authentication/authorization are future work. The provided application is intended for local learning and should not be deployed publicly as-is.
