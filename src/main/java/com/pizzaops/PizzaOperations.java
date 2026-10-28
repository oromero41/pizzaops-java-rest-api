package com.pizzaops;

import com.pizzaops.PizzaCatalog.*;
import java.util.List;
import java.util.Optional;

/** Contract shared by memory-backed CLI/tests and database-backed REST API. */
public interface PizzaOperations {
    PizzaOrder createOrder(Size size, Base base, Sauce sauce, Cheese cheese, List<Topping> toppings);
    Optional<PizzaOrder> getOrder(long id);
    List<PizzaOrder> getOrders();
    PizzaStatistics getStatistics();
}
