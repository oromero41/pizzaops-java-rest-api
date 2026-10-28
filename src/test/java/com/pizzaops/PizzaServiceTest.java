package com.pizzaops;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import com.pizzaops.PizzaCatalog.*;

/** No-dependency regression tests; a JUnit migration is planned for phase 2. */
public final class PizzaServiceTest {
    private static int checks = 0;

    @Test
    void originalRegressionSuite() {
        main(new String[0]);
    }

    public static void main(String[] args) {
        emptyStatisticsAreSafe();
        confirmedOrdersCountOnceAndHaveSequentialIds();
        allStatisticsAndAverageAreCorrect();
        surchargeOnlyAfterFourToppings();
        rejectedDuplicateDoesNotSaveOrder();
        callerCannotChangeSavedOrder();
        returnedOrderListCannotBeEdited();
        invalidInputIsRejectedAndRecovered();
        canceledOrderDoesNotAffectStatistics();
        confirmedCliOrderUpdatesStatistics();
        exitConfirmationIsHandled();
        System.out.println("PASS: " + checks + " assertions across 11 test scenarios.");
    }

    private static PizzaOrder place(PizzaService service, List<Topping> toppings) {
        return service.createOrder(Size.SMALL, Base.ORIGINAL, Sauce.TOMATO, Cheese.MOZZARELLA, toppings);
    }

    private static void emptyStatisticsAreSafe() {
        PizzaStatistics stats = new PizzaService().getStatistics();
        check(stats.orderCount() == 0, "empty order count");
        check(stats.averageToppings() == 0.0, "empty average");
        check(stats.sizeCounts().get(Size.SMALL) == 0, "empty categories show zero");
    }

    private static void confirmedOrdersCountOnceAndHaveSequentialIds() {
        PizzaService service = new PizzaService();
        check(place(service, List.of()).id() == 1, "first id");
        check(place(service, List.of(Topping.HAM)).id() == 2, "second id");
        check(service.getStatistics().orderCount() == 2, "order counted exactly once");
        check(service.getStatistics().sizeCounts().get(Size.SMALL) == 2, "size frequency");
    }

    private static void allStatisticsAndAverageAreCorrect() {
        PizzaService service = new PizzaService();
        place(service, List.of(Topping.PEPPERONI, Topping.HAM, Topping.OLIVES, Topping.MUSHROOM));
        place(service, List.of(Topping.HAM, Topping.OLIVES));
        PizzaStatistics stats = service.getStatistics();
        check(stats.averageToppings() == 3.0, "average of 4 and 2 = 3");
        check(stats.toppingCounts().get(Topping.HAM) == 2, "ham counted twice");
        check(stats.toppingCounts().get(Topping.PEPPERONI) == 1, "pepperoni counted once");
        check(stats.cheeseCounts().get(Cheese.MOZZARELLA) == 2, "cheese frequency");
        check(stats.sauceCounts().get(Sauce.TOMATO) == 2, "sauce frequency");
    }

    private static void surchargeOnlyAfterFourToppings() {
        PizzaService service = new PizzaService();
        check(place(service, List.of()).extraToppingChargeGbp() == 0, "zero toppings free");
        check(place(service, List.of(Topping.HAM, Topping.BEEF, Topping.OLIVES,
                Topping.PEPPER)).extraToppingChargeGbp() == 0, "four included");
        check(place(service, List.of(Topping.HAM, Topping.BEEF, Topping.OLIVES,
                Topping.PEPPER, Topping.CHICKEN)).extraToppingChargeGbp() == 1, "fifth costs £1");
        check(service.getStatistics().totalExtraChargesGbp() == 1, "total extra charges");
    }

    private static void rejectedDuplicateDoesNotSaveOrder() {
        PizzaService service = new PizzaService();
        expectIllegalArgument(() -> place(service, List.of(Topping.HAM, Topping.HAM)));
        check(service.getOrders().isEmpty(), "duplicate rejected before saving");
        check(place(service, List.of()).id() == 1, "rejected order does not increment id");
    }

    private static void callerCannotChangeSavedOrder() {
        PizzaService service = new PizzaService();
        List<Topping> editable = new ArrayList<>(List.of(Topping.CHICKEN));
        PizzaOrder order = place(service, editable);
        editable.add(Topping.HAM);
        check(order.toppings().size() == 1, "defensive copy");
        try {
            order.toppings().add(Topping.BEEF);
            throw new AssertionError("toppings list must be immutable");
        } catch (UnsupportedOperationException expected) {
            checks++;
        }
    }

    private static void returnedOrderListCannotBeEdited() {
        PizzaService service = new PizzaService();
        place(service, List.of(Topping.OLIVES));
        try {
            service.getOrders().clear();
            throw new AssertionError("order list must be immutable");
        } catch (UnsupportedOperationException expected) {
            checks++;
        }
    }

    private static void invalidInputIsRejectedAndRecovered() {
        String output = runCli("text\n9\n2\n3\nY\n");
        check(output.contains("Invalid selection. Enter a number from 1 to 3."), "invalid menu input");
        check(output.contains("Confirmed orders: 0"), "valid menu after invalid input");
        check(output.contains("Goodbye!"), "exit after invalid input");
    }

    private static void canceledOrderDoesNotAffectStatistics() {
        String output = runCli("1\n1\n1\n1\n1\n1\n0\nN\n2\n3\nY\n");
        check(output.contains("Order canceled. No statistics were changed."), "canceled order message");
        check(output.contains("Confirmed orders: 0"), "canceled order not counted");
    }

    private static void confirmedCliOrderUpdatesStatistics() {
        String output = runCli("1\n1\n1\n1\n1\n1\n2\n3\n4\n0\nY\n2\n3\nY\n");
        check(output.contains("Order #1 confirmed"), "order confirmation and ID");
        check(output.contains("Confirmed orders: 1"), "confirmed order counted once");
        check(output.contains("Average toppings per order: 4.00"), "CLI average is correct");
        check(output.contains("Total extra topping charges: GBP 0"), "four toppings included");
    }

    private static void exitConfirmationIsHandled() {
        String output = runCli("3\nmaybe\nN\n2\n3\nY\n");
        check(output.contains("Invalid selection. Enter Y or N."), "invalid exit confirmation");
        check(output.contains("Confirmed orders: 0"), "cancel exit returns to menu");
    }

    private static String runCli(String input) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new PizzaOrderSystem(new Scanner(input)).run();
        } finally {
            System.setOut(previous);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            checks++;
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
