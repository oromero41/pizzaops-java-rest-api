package com.pizzaops.api;

import com.pizzaops.PizzaCatalog.*;
import java.util.Arrays;
import java.util.List;

public record CatalogResponse(List<Option> sizes, List<Option> bases,
                              List<Option> sauces, List<Option> cheeses,
                              List<Option> toppings) {
    public record Option(String code, String label, String category) { }

    public static CatalogResponse fromCatalog() {
        return new CatalogResponse(
            Arrays.stream(Size.values()).map(x -> new Option(x.name(), x.label(), null)).toList(),
            Arrays.stream(Base.values()).map(x -> new Option(x.name(), x.label(), null)).toList(),
            Arrays.stream(Sauce.values()).map(x -> new Option(x.name(), x.label(), null)).toList(),
            Arrays.stream(Cheese.values()).map(x -> new Option(x.name(), x.label(), null)).toList(),
            Arrays.stream(Topping.values()).map(x -> new Option(x.name(), x.label(), x.category())).toList()
        );
    }
}
