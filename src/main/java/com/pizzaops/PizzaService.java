package com.pizzaops;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.pizzaops.PizzaCatalog.*;

/** Business logic independent of the keyboard interface, ready for reuse by an API. */
public final class PizzaService implements PizzaOperations {
    public static final int INCLUDED_TOPPINGS = 4;
    public static final int EXTRA_TOPPING_PRICE_GBP = 1;

    private final List<PizzaOrder> orders = new ArrayList<>();
    private long nextOrderId = 1;

    public synchronized PizzaOrder createOrder(Size size, Base base, Sauce sauce, Cheese cheese,
                                  List<Topping> toppings) {
        int charge = validateAndCalculateCharge(size, base, sauce, cheese, toppings);
        PizzaOrder order = new PizzaOrder(nextOrderId++, size, base, sauce, cheese, toppings, charge);
        orders.add(order);
        return order;
    }

    public synchronized Optional<PizzaOrder> getOrder(long id) {
        return orders.stream().filter(order -> order.id() == id).findFirst();
    }

    public synchronized List<PizzaOrder> getOrders() {
        return List.copyOf(orders);
    }

    public synchronized PizzaStatistics getStatistics() {
        return calculateStatistics(orders);
    }

    /** Counts are computed from confirmed orders (whether loaded from RAM or PostgreSQL). */
    public static PizzaStatistics calculateStatistics(List<PizzaOrder> orders) {
        EnumMap<Size, Integer> sizes = zeroCounts(Size.class);
        EnumMap<Base, Integer> bases = zeroCounts(Base.class);
        EnumMap<Sauce, Integer> sauces = zeroCounts(Sauce.class);
        EnumMap<Cheese, Integer> cheeses = zeroCounts(Cheese.class);
        EnumMap<Topping, Integer> toppings = zeroCounts(Topping.class);
        int totalToppings = 0;
        int totalCharges = 0;

        for (PizzaOrder order : orders) {
            sizes.merge(order.size(), 1, Integer::sum);
            bases.merge(order.base(), 1, Integer::sum);
            sauces.merge(order.sauce(), 1, Integer::sum);
            cheeses.merge(order.cheese(), 1, Integer::sum);
            for (Topping topping : order.toppings()) {
                toppings.merge(topping, 1, Integer::sum);
            }
            totalToppings += order.toppings().size();
            totalCharges += order.extraToppingChargeGbp();
        }
        double average = orders.isEmpty() ? 0.0 : (double) totalToppings / orders.size();
        return new PizzaStatistics(orders.size(), sizes, bases, sauces, cheeses,
                toppings, average, totalCharges);
    }

    /** Validate before persistence so bad requests never create a database row. */
    public static int validateAndCalculateCharge(Size size, Base base, Sauce sauce, Cheese cheese,
                                                 List<Topping> toppings) {
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(sauce, "sauce");
        Objects.requireNonNull(cheese, "cheese");
        Objects.requireNonNull(toppings, "toppings");
        EnumSet<Topping> unique = EnumSet.noneOf(Topping.class);
        for (Topping topping : toppings) {
            Objects.requireNonNull(topping, "topping");
            if (!unique.add(topping)) {
                throw new IllegalArgumentException("Duplicate topping: " + topping.label());
            }
        }
        return Math.max(0, toppings.size() - INCLUDED_TOPPINGS) * EXTRA_TOPPING_PRICE_GBP;
    }

    private static <E extends Enum<E>> EnumMap<E, Integer> zeroCounts(Class<E> enumType) {
        EnumMap<E, Integer> counts = new EnumMap<>(enumType);
        for (E key : enumType.getEnumConstants()) {
            counts.put(key, 0);
        }
        return counts;
    }
}
