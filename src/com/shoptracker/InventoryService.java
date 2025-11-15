package com.shoptracker;

import java.util.*;

/**
 * Central inventory system for products, stock levels, and history tracking.
 * SonarQube-safe, fully deterministic, and permission-controlled.
 */
public final class InventoryService {

    // ---------------- SINGLETON ----------------
    private static InventoryService INSTANCE;

    public static synchronized InventoryService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InventoryService(AccessControl.getInstance());
        }
        return INSTANCE;
    }

    // ---------------- FIELDS ----------------
    private final AccessControl accessControl;
    private final Map<String, Product> products = new HashMap<>();
    private final List<InventoryEvent> history = new ArrayList<>();

    private final Map<String, Integer> restockThresholds = new HashMap<>();
    private static final int DEFAULT_THRESHOLD = 5;
    private static final String SYSTEM_USER = "system";


    // ---------------- CONSTRUCTOR ----------------
    public InventoryService(AccessControl accessControl) {
        this.accessControl = Objects.requireNonNull(accessControl);
    }

    // ---------------- PRODUCT CRUD (ADMIN / MANAGER ONLY) ----------------

    public boolean addProduct(User actor, Product product) {
        if (!accessControl.canManageStock(actor)) {
            return false;
        }
        if (product == null) {
            return false;
        }

        products.put(product.getId(), product);

        recordEvent(
                product.getId(),
                product.getName(),
                actor.getUsername(),
                InventoryEvent.EventType.ADD,
                0,
                product.getQuantity()
        );

        return true;
    }

    public boolean removeProduct(User actor, String id) {
        if (!accessControl.canManageStock(actor)) {
            return false;
        }

        Product removed = products.remove(id);
        if (removed == null) {
            return false;
        }

        recordEvent(
                id,
                removed.getName(),
                actor.getUsername(),
                InventoryEvent.EventType.REMOVE,
                removed.getQuantity(),
                0
        );

        return true;
    }

    public boolean updateProduct(User actor, String id, int qty, double price) {
        if (!accessControl.canManageStock(actor)) {
            return false;
        }

        Product p = products.get(id);
        if (p == null) {
            return false;
        }

        int oldQty = p.getQuantity();
        p.setQuantity(qty);
        p.setPrice(price);

        recordEvent(
                id,
                p.getName(),
                actor.getUsername(),
                InventoryEvent.EventType.SET,
                oldQty,
                qty
        );

        return true;
    }

    // ---------------- GETTERS ----------------

    public Product getProduct(String id) {
        return products.get(id);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public void clearInventory() {
        products.clear();
        history.clear();
    }

    // ---------------- SEARCH ----------------

    public List<Product> searchByName(String name) {
        if (name == null) {
            return Collections.emptyList();
        }

        String lower = name.toLowerCase();
        List<Product> result = new ArrayList<>();

        for (Product p : products.values()) {
            if (p.getName().toLowerCase().contains(lower)) {
                result.add(p);
            }
        }
        return result;
    }

    // ---------------- STOCK ADJUSTMENT (USER + ADMIN) ----------------

    /**
     * USERS are allowed to adjust stock (only +1 or -1 in UI).
     * ADMIN/MANAGER also allowed.
     */
    public boolean adjustQuantity(User actor, String id, int delta) {
        Product p = products.get(id);
        if (p == null) {
            return false;
        }

        boolean hasPermission =
                accessControl.canManageStock(actor) ||
                Role.USER.equals(actor.getRole()); // allow basic user

        if (!hasPermission) {
            return false;
        }

        int oldQty = p.getQuantity();
        int newQty = oldQty + delta;

        if (newQty < 0) {
            return false;
        }

        p.setQuantity(newQty);

        recordEvent(
                id,
                p.getName(),
                actor.getUsername(),
                InventoryEvent.EventType.ADJUST,
                oldQty,
                newQty
        );

        return true;
    }

    // Increase by system (used by UI)
    public boolean increaseStock(String id) {
        Product p = products.get(id);
        if (p == null) {
            return false;
        }

        int oldQty = p.getQuantity();
        int newQty = oldQty + 1;
        p.setQuantity(newQty);

        recordEvent(
                id,
                p.getName(),
                SYSTEM_USER,
                InventoryEvent.EventType.INCREASE,
                oldQty,
                newQty
        );
        return true;
    }

    public boolean decreaseStock(String id) {
        Product p = products.get(id);
        if (p == null || p.getQuantity() <= 0) {
            return false;
        }

        int oldQty = p.getQuantity();
        int newQty = oldQty - 1;
        p.setQuantity(newQty);

        recordEvent(
                id,
                p.getName(),
                SYSTEM_USER,
                InventoryEvent.EventType.DECREASE,
                oldQty,
                newQty
        );
        return true;
    }

    // ---------------- RESTOCK / LOW STOCK ----------------

    public List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();

        for (Product p : products.values()) {
            if (p.getQuantity() <= getRestockThreshold(p.getId())) {
                list.add(p);
            }
        }
        return list;
    }

    public int getRestockThreshold(String id) {
        return restockThresholds.getOrDefault(id, DEFAULT_THRESHOLD);
    }

    public int getSuggestedRestockQuantity(Product p) {
        int threshold = getRestockThreshold(p.getId());
        return Math.max(0, (threshold * 2) - p.getQuantity());
    }

    public void setRestockThreshold(User actor, String id, int threshold) {
        if (!accessControl.canManageStock(actor)) {
            return;
        }
        restockThresholds.put(id, Math.max(0, threshold));
    }

    // ---------------- HISTORY ----------------

    public List<InventoryEvent> getHistory() {
        return new ArrayList<>(history);
    }

    private void recordEvent(String id,
                             String name,
                             String username,
                             InventoryEvent.EventType type,
                             int oldQty,
                             int newQty) {

        history.add(new InventoryEvent(
                id,
                name,
                username,
                type,
                oldQty,
                newQty
        ));
    }

    // ---------------- DEFAULT STOCK ----------------

    public void seedDefaultStockIfEmpty() {
        if (!products.isEmpty()) {
            return;
        }

        User system = new User(SYSTEM_USER, "1234", SYSTEM_USER, "system@local", Role.ADMIN);

        addProduct(system, new Product("A01", "Apples", 20, 0.50));
        addProduct(system, new Product("B01", "Bananas", 30, 0.40));
        addProduct(system, new Product("O01", "Oranges", 25, 0.60));
    }
}
