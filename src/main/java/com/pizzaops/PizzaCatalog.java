package com.pizzaops;

/** The menu values are defined once and reused by the CLI and service. */
public final class PizzaCatalog {
    private PizzaCatalog() { }

    public enum Size {
        SMALL("Small"), MEDIUM("Medium"), LARGE("Large");
        private final String label;
        Size(String label) { this.label = label; }
        public String label() { return label; }
    }

    public enum Base {
        ORIGINAL("Original"), THIN("Thin Crust"), THICK("Thick Crust"),
        STUFFED("Stuffed Crust (with Cheese)");
        private final String label;
        Base(String label) { this.label = label; }
        public String label() { return label; }
    }

    public enum Sauce {
        TOMATO("Tomato"), BARBEQUE("Barbeque");
        private final String label;
        Sauce(String label) { this.label = label; }
        public String label() { return label; }
    }

    public enum Cheese {
        MOZZARELLA("Mozzarella"), LOW_FAT("Lowfat"), VEGAN("Vegan");
        private final String label;
        Cheese(String label) { this.label = label; }
        public String label() { return label; }
    }

    public enum Topping {
        PEPPERONI("Pepperoni", "Meat"), HAM("Ham", "Meat"), BEEF("Beef", "Meat"),
        CHICKEN("Chicken", "Meat"), SAUSAGE("Sausage", "Meat"), BACON("Bacon", "Meat"),
        PEPPER("Pepper", "Veg"), OLIVES("Olives", "Veg"), ONIONS("Onions", "Veg"),
        TOMATO("Tomato", "Veg"), SWEETCORN("Sweetcorn", "Veg"),
        PINEAPPLE("Pineapple", "Veg"), MUSHROOM("Mushroom", "Veg");
        private final String label;
        private final String category;
        Topping(String label, String category) {
            this.label = label;
            this.category = category;
        }
        public String label() { return label; }
        public String category() { return category; }
    }
}
