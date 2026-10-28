# Troubleshooting case studies

These are hands-on learning incidents encountered while developing and testing PizzaOps locally. They are **not** incidents from a production employer.

## Case 1 — API start failure after adding PostgreSQL

**Symptom:** Spring Boot exited with Maven's `BUILD FAILURE` and `exit code 1`, even after the PostgreSQL password was entered.

**Investigation:** Read the first `Caused by:` lines rather than relying on the final Maven error. The nested cause identified:

```text
Cannot subclass final class com.pizzaops.JdbcPizzaService
```

**Root cause:** `JdbcPizzaService` was declared `final`. Spring's transaction support needed to create a CGLIB-based subclass, which Java disallows for `final` classes. The password was not the cause of this startup failure.

**Resolution:** Change:

```java
public final class JdbcPizzaService implements PizzaOperations {
```

to:

```java
public class JdbcPizzaService implements PizzaOperations {
```

**Verification:** Spring Boot started, a `POST /api/v1/orders` returned `201 Created`, the order appeared in PostgreSQL via `SELECT * FROM orders`, and orders could be retrieved after an API restart.

**Preventive action:** Include a startup/context test that covers the actual database-backed profile in a future iteration. Existing automated API tests run only with the in-memory `test` profile.

## Case 2 — Invalid pizza size returned an ambiguous error

**Symptom:** Posting valid JSON with `"size": "EXTRA_GIGANTE"` originally returned a generic `400 INVALID_JSON` response.

**Investigation:** The JSON syntax was valid; the size value did not match the Java catalog enum.

**Root cause:** The HTTP error handler grouped invalid catalog values with malformed JSON.

**Resolution:** Handle Jackson enum-conversion errors separately, returning `INVALID_CATALOG_VALUE`, the affected `field`, and `allowedValues`.

**Verification:** Postman returned `400` and `field: size` with allowed sizes. Two Postman assertions passed, and the invalid order was not persisted.

## Practical API support checklist

1. Reproduce the request with the correct HTTP method (`GET` vs `POST`).
2. Verify the endpoint, headers, JSON body and response status.
3. Inspect `error`, `field` and `allowedValues` in the response.
4. Check whether the database contains the expected row using read-only SQL.
5. Inspect the earliest `Caused by:` entry in a server stack trace when startup fails.
6. Record the root cause, resolution and verification evidence without including credentials.
