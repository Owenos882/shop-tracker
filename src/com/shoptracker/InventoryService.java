package com.shoptracker;

import java.util.*;

public class InventoryService {

    private static final InventoryService INSTANCE =
            new InventoryService(AccessControl.getInstance());

    public static InventoryService getInstance() {
        return INSTANCE;
    }

    private final AccessControl accessControl;
    private final ActivityLogService logService;
    private final Map<String, Product> products = new HashMap<>();
    private boolean defaultStockLoaded = false;

    private InventoryService(AccessControl accessControl) {
        this.accessControl = accessControl;
        this.logService = ActivityLogService.getInstance();
    }

    /**
     * Load default grocery stock only once (first use).
     * Will not overwrite existing data.
     */
    public synchronized void seedDefaultStockIfEmpty() {
        if (defaultStockLoaded) {
            return;
        }
        if (!products.isEmpty()) {
            defaultStockLoaded = true;
            return;
        }

        addDefaultProduct("A001", "Apples (bag)", 20, 1.00);
        addDefaultProduct("A002", "Bananas (bunch)", 15, 1.10);
        addDefaultProduct("A003", "Milk (1L)", 10, 1.50);
        addDefaultProduct("A004", "Bread Loaf", 8, 1.80);
        addDefaultProduct("A005", "Eggs (12 pack)", 30, 2.50);

        defaultStockLoaded = true;
        logService.log("Default stock seeded.");
    }

    private void addDefaultProduct(String id, String name, int qty, double price) {
        Product p = new Product(id, name, qty, price);
        products.put(id, p);
        logService.log("Default product added: " + id + " (" + name + "), qty=" + qty + ", price=" + price);
    }

    // ----------------- Admin/Manager stock management -----------------

    public boolean addProduct(User actor, Product product) {
        if (!accessControl.canManageStock(actor)) {
            logService.log("ACCESS DENIED: " + safeUser(actor)
                    + " tried to add product " + product.getId());
            return false;
        }
        products.put(product.getId(), product);
        logService.log("Product added: " + product.getId()
                + " (" + product.getName() + ") by " + safeUser(actor));
        return true;
    }

    public boolean removeProduct(User actor, String id) {
        if (!accessControl.canManageStock(actor)) {
            logService.log("ACCESS DENIED: " + safeUser(actor)
                    + " tried to remove product " + id);
            return false;
        }
        Product removed = products.remove(id);
        if (removed != null) {
            logService.log("Product removed: " + id + " by " + safeUser(actor));
            return true;
        }
        logService.log("Product removal FAILED, not found: " + id);
        return false;
    }

    public boolean updateProduct(User actor, String id, int qty, double price) {
        if (!accessControl.canManageStock(actor)) {
            logService.log("ACCESS DENIED: " + safeUser(actor)
                    + " tried to update product " + id);
            return false;
        }
        Product p = products.get(id);
        if (p == null) {
            logService.log("Product update FAILED, not found: " + id);
            return false;
        }
        p.setQuantity(qty);
        p.setPrice(price);
        logService.log("Product updated: " + id + " qty=" + qty + " price=" + price
                + " by " + safeUser(actor));
        return true;
    }

    // ----------------- Any user can adjust quantity via +/- -----------------

    /**
     * Adjust quantity by delta (e.g. +1 or -1) for any logged-in user.
     * Does NOT allow stock to go below zero.
     * Returns true if adjustment was successful.
     */
    public boolean adjustQuantity(User actor, String id, int delta) {
        Product p = products.get(id);
        if (p == null) {
            logService.log("Quantity adjust FAILED, product not found: " + id);
            return false;
        }
        int newQty = p.getQuantity() + delta;
        if (newQty < 0) {
            logService.log("Quantity adjust FAILED, below zero: " + id
                    + " requested delta=" + delta);
            return false;
        }
        p.setQuantity(newQty);
        logService.log("Quantity adjusted for " + id + " by " + safeUser(actor)
                + ": delta=" + delta + ", newQty=" + newQty);
        return true;
    }

    // ----------------- Queries / utilities -----------------

    public Product getProduct(String id) {
        return products.get(id);
    }

    public List<Product> getAllProducts() {
        return List.copyOf(products.values());
    }

    public int size() {
        return products.size();
    }

    public List<Product> searchByName(String name) {
        String lower = name.toLowerCase();
        List<Product> result = new ArrayList<>();
        for (Product p : products.values()) {
            if (p.getName().toLowerCase().contains(lower)) {
                result.add(p);
            }
        }
        return result;
    }

    public void clearInventory() {
        products.clear();
        logService.log("Inventory cleared");
    }

    private String safeUser(User user) {
        return (user == null) ? "<null>" : user.getUsername();
    }
}
