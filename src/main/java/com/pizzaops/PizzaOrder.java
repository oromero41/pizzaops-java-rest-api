package com.pizzaops;

import java.util.List;
import java.util.Objects;
import com.pizzaops.PizzaCatalog.*;

/** One confirmed order. The list is copied to prevent accidental changes. */
public record PizzaOrder(long id, Size size, Base base, Sauce sauce, Cheese cheese,
                         List<Topping> toppings, int extraToppingChargeGbp) {
    public PizzaOrder {
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(sauce, "sauce");
        Objects.requireNonNull(cheese, "cheese");
        toppings = List.copyOf(Objects.requireNonNull(toppings, "toppings"));
        if (extraToppingChargeGbp < 0) {
            throw new IllegalArgumentException("Extra topping charge cannot be negative");
        }
    }
}
