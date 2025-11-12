package com.shoptracker;

public class Product {
    private final String id;
    private final String name;
    private int quantity;
    private double price;

    public Product(String id, String name, int quantity, double price) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Product ID required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name required");
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")  Qty: " + quantity + "  €" + price;
    }
}
