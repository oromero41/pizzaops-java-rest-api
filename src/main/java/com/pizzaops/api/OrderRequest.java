package com.pizzaops.api;

import com.pizzaops.PizzaCatalog.*;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Strict JSON input contract. Enums use their UPPERCASE codes. */
public record OrderRequest(
        @NotNull Size size,
        @NotNull Base base,
        @NotNull Sauce sauce,
        @NotNull Cheese cheese,
        @NotNull List<@NotNull Topping> toppings) { }
