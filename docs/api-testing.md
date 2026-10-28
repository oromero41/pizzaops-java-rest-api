# API verification with Postman

Import `postman/PizzaOps_2_0.postman_collection.json` from the project into Postman. It uses a collection variable `baseUrl` set to `http://localhost:8080`.

## Positive scenario

1. `GET /api/v1/menu` → `200 OK` with catalog options.
2. `POST /api/v1/orders` with valid JSON → `201 Created` with an order ID.
3. `GET /api/v1/orders` → `200 OK`, including the created order.
4. `GET /api/v1/statistics` → `200 OK` with order counts.
5. In pgAdmin run `SELECT * FROM orders ORDER BY id DESC;` to verify persistence.
6. Restart Spring Boot, repeat `GET /api/v1/orders`, and confirm orders are still present.

## Negative scenario

Change only `size` to `EXTRA_GIGANTE`. The server should return `400` with `error = INVALID_CATALOG_VALUE`, `field = size` and the list of accepted sizes. Verify the order count does not increase.

Postman post-response test (only for the **invalid-size** request):

```javascript
pm.test('Invalid size returns HTTP 400', function () {
  pm.response.to.have.status(400);
});
pm.test('Error identifies invalid size', function () {
  const data = pm.response.json();
  pm.expect(data.error).to.eql('INVALID_CATALOG_VALUE');
  pm.expect(data.field).to.eql('size');
});
```

**Important:** Don't leave these assertions on a valid-order request. A successful `POST` returns `201`, so tests expecting `400` will fail correctly even when the API is working.

## Test coverage boundary

The included Spring MVC integration tests run against an **in-memory service**. They test controller behavior and structured error responses, but do not check JDBC/PostgreSQL behavior. Database persistence was verified manually during the local build.
