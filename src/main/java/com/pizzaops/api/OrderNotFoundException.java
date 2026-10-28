package com.pizzaops.api;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("Order " + id + " was not found");
    }
}
