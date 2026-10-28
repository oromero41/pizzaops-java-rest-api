package com.pizzaops;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaops.PizzaCatalog.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

/** PostgreSQL implementation of PizzaOperations. The CLI remains in-memory. */
public class JdbcPizzaService implements PizzaOperations {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final RowMapper<PizzaOrder> orderMapper = this::readOrder;

    public JdbcPizzaService(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PizzaOrder createOrder(Size size, Base base, Sauce sauce, Cheese cheese,
                                  List<Topping> toppings) {
        int charge = PizzaService.validateAndCalculateCharge(size, base, sauce, cheese, toppings);
        try {
            // PostgreSQL's explicit JSONB cast maps the JSON text to the user's existing table.
            String toppingsJson = mapper.writeValueAsString(toppings);
            Long id = jdbc.queryForObject("""
                    INSERT INTO orders (size, base, sauce, cheese, toppings, extra_topping_charge_gbp)
                    VALUES (?, ?, ?, ?, CAST(? AS jsonb), ?)
                    RETURNING id
                    """, Long.class,
                    size.name(), base.name(), sauce.name(), cheese.name(), toppingsJson, charge);
            if (id == null) throw new IllegalStateException("Database did not return the order ID");
            return new PizzaOrder(id, size, base, sauce, cheese, toppings, charge);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize pizza toppings", e);
        }
    }

    @Override
    public Optional<PizzaOrder> getOrder(long id) {
        return jdbc.query("""
                SELECT id, size, base, sauce, cheese, toppings, extra_topping_charge_gbp
                FROM orders WHERE id = ?
                """, orderMapper, id).stream().findFirst();
    }

    @Override
    public List<PizzaOrder> getOrders() {
        return jdbc.query("""
                SELECT id, size, base, sauce, cheese, toppings, extra_topping_charge_gbp
                FROM orders ORDER BY id
                """, orderMapper);
    }

    @Override
    public PizzaStatistics getStatistics() {
        return PizzaService.calculateStatistics(getOrders());
    }

    private PizzaOrder readOrder(ResultSet rs, int rowNum) throws SQLException {
        try {
            List<Topping> toppings = mapper.readValue(rs.getString("toppings"),
                    new TypeReference<List<Topping>>() {});
            return new PizzaOrder(rs.getLong("id"),
                    Size.valueOf(rs.getString("size")),
                    Base.valueOf(rs.getString("base")),
                    Sauce.valueOf(rs.getString("sauce")),
                    Cheese.valueOf(rs.getString("cheese")),
                    toppings,
                    rs.getInt("extra_topping_charge_gbp"));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new SQLException("Cannot deserialize database order " + rs.getLong("id"), e);
        }
    }
}
