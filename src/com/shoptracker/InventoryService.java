package com.shoptracker;

import java.util.*;

public class InventoryService {
    // Static inventory so it's shared between all instances
    private static final Map<String, Product> inventory = new HashMap<>();
    public InventoryService(AccessControl accessControl) {
    }

    public boolean addProduct(User user, Product product) {
        if (!AccessControl.canManageStock(user)) return false;
        if (inventory.containsKey(product.getId())) return false;
        inventory.put(product.getId(), product);
        return true;
    }

    public boolean updateProduct(User user, String id, int newQty, double newPrice) {
        if (!AccessControl.canManageStock(user)) return false;
        Product p = inventory.get(id);
        if (p == null) return false;
        p.setQuantity(newQty);
        p.setPrice(newPrice);
        return true;
    }

    public boolean removeProduct(User user, String id) {
        if (!AccessControl.canManageStock(user)) return false;
        return inventory.remove(id) != null;
    }

    public Product getProduct(String id) {
        return inventory.get(id);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(inventory.values());
    }

    public List<Product> searchByName(String name) {
        List<Product> results = new ArrayList<>();
        for (Product p : inventory.values()) {
            if (p.getName().toLowerCase().contains(name.toLowerCase())) {
                results.add(p);
            }
        }
        return results;
    }

    public int size() {
        return inventory.size();
    }

    // Optional clear method for testing
    public void clearInventory() {
        inventory.clear();
    }
}
