package com.pizzaops;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.pizzaops.PizzaCatalog.*;

/** Simple command-line interface. All order state lives in PizzaService. */
public final class PizzaOrderSystem {
    private final Scanner input;
    private final PizzaService service = new PizzaService();

    public PizzaOrderSystem(Scanner input) {
        this.input = input;
    }

    public static void main(String[] args) {
        new PizzaOrderSystem(new Scanner(System.in)).run();
    }

    public void run() {
        System.out.println("PizzaOps 2.0 - Order Management (Phase 1)");
        while (true) {
            System.out.println("\n1. Order a pizza\n2. View statistics\n3. Exit");
            Integer choice = readNumber("Select an option (1-3): ", 1, 3);
            if (choice == null) break;
            switch (choice) {
                case 1 -> placeOrder();
                case 2 -> printStatistics();
                case 3 -> {
                    Boolean exit = readYesNo("Exit the program? (Y/N): ");
                    if (Boolean.TRUE.equals(exit) || exit == null) {
                        System.out.println("Goodbye!");
                        return;
                    }
                }
                default -> throw new IllegalStateException("Unreachable menu option");
            }
        }
        System.out.println("Input closed. Goodbye!");
    }

    private void placeOrder() {
        Size size = choose("Size", Size.values());
        if (size == null) return;
        Base base = choose("Base", Base.values());
        if (base == null) return;
        Sauce sauce = choose("Sauce", Sauce.values());
        if (sauce == null) return;
        Cheese cheese = choose("Cheese", Cheese.values());
        if (cheese == null) return;

        List<Topping> toppings = new ArrayList<>();
        Topping[] choices = Topping.values();
        while (true) {
            System.out.println("\nAvailable toppings (first four are included):");
            for (int i = 0; i < choices.length; i++) {
                System.out.printf("%d. %s (%s)%n", i + 1, choices[i].label(), choices[i].category());
            }
            Integer selection = readNumber("Choose a topping (0 to finish): ", 0, choices.length);
            if (selection == null) return;
            if (selection == 0) break;
            Topping topping = choices[selection - 1];
            if (toppings.contains(topping)) {
                System.out.println("That topping is already selected; choose another.");
            } else {
                toppings.add(topping);
                System.out.println("Added: " + topping.label());
            }
            if (toppings.size() == choices.length) break;
        }

        int surcharge = Math.max(0, toppings.size() - PizzaService.INCLUDED_TOPPINGS);
        System.out.println("\nReview your order:");
        System.out.printf("%s | %s | %s | %s%n", size.label(), base.label(),
                sauce.label(), cheese.label());
        System.out.println("Toppings: " + (toppings.isEmpty() ? "None" : toppings));
        System.out.printf("Extra topping charge: GBP %d%n", surcharge);
        Boolean confirmed = readYesNo("Confirm order? (Y/N): ");
        if (confirmed == null || !confirmed) {
            System.out.println("Order canceled. No statistics were changed.");
            return;
        }
        PizzaOrder order = service.createOrder(size, base, sauce, cheese, toppings);
        System.out.printf("Order #%d confirmed. Extra topping charge: GBP %d%n",
                order.id(), order.extraToppingChargeGbp());
    }

    private void printStatistics() {
        PizzaStatistics stats = service.getStatistics();
        System.out.printf("\nConfirmed orders: %d%n", stats.orderCount());
        System.out.printf("Average toppings per order: %.2f%n", stats.averageToppings());
        System.out.printf("Total extra topping charges: GBP %d%n", stats.totalExtraChargesGbp());
        System.out.println("\nSizes:");
        for (Size value : Size.values()) {
            System.out.printf("  %-24s %d%n", value.label(), stats.sizeCounts().get(value));
        }
        System.out.println("Bases:");
        for (Base value : Base.values()) {
            System.out.printf("  %-24s %d%n", value.label(), stats.baseCounts().get(value));
        }
        System.out.println("Sauces:");
        for (Sauce value : Sauce.values()) {
            System.out.printf("  %-24s %d%n", value.label(), stats.sauceCounts().get(value));
        }
        System.out.println("Cheeses:");
        for (Cheese value : Cheese.values()) {
            System.out.printf("  %-24s %d%n", value.label(), stats.cheeseCounts().get(value));
        }
        System.out.println("Toppings:");
        for (Topping value : Topping.values()) {
            System.out.printf("  %-24s %d%n", value.label(), stats.toppingCounts().get(value));
        }
    }

    private <E extends Enum<E>> E choose(String label, E[] options) {
        System.out.println("\n" + label + ":");
        for (int i = 0; i < options.length; i++) {
            String name = options[i] instanceof Size s ? s.label()
                    : options[i] instanceof Base b ? b.label()
                    : options[i] instanceof Sauce s ? s.label()
                    : options[i] instanceof Cheese c ? c.label()
                    : options[i].name();
            System.out.printf("%d. %s%n", i + 1, name);
        }
        Integer selection = readNumber("Select (1-" + options.length + "): ", 1, options.length);
        return selection == null ? null : options[selection - 1];
    }

    private Integer readNumber(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!input.hasNextLine()) return null;
            String raw = input.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // Show the same helpful message for text and out-of-range numbers.
            }
            System.out.printf("Invalid selection. Enter a number from %d to %d.%n", min, max);
        }
    }

    private Boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!input.hasNextLine()) return null;
            String raw = input.nextLine().trim();
            if (raw.equalsIgnoreCase("Y")) return true;
            if (raw.equalsIgnoreCase("N")) return false;
            System.out.println("Invalid selection. Enter Y or N.");
        }
    }
}
