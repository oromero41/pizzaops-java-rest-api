package com.pizzaops;

import java.util.Map;
import com.pizzaops.PizzaCatalog.*;

/** Snapshot of order statistics, calculated only from confirmed orders. */
public record PizzaStatistics(
        int orderCount,
        Map<Size, Integer> sizeCounts,
        Map<Base, Integer> baseCounts,
        Map<Sauce, Integer> sauceCounts,
        Map<Cheese, Integer> cheeseCounts,
        Map<Topping, Integer> toppingCounts,
        double averageToppings,
        int totalExtraChargesGbp) {
    public PizzaStatistics {
        sizeCounts = Map.copyOf(sizeCounts);
        baseCounts = Map.copyOf(baseCounts);
        sauceCounts = Map.copyOf(sauceCounts);
        cheeseCounts = Map.copyOf(cheeseCounts);
        toppingCounts = Map.copyOf(toppingCounts);
    }
}
