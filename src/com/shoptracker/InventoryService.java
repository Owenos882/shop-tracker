package com.shoptracker;

import java.time.LocalDateTime;
import java.util.*;

/**
 * FINAL stable InventoryService with:
 * - CRUD operations
 * - History via InventoryEvent
 * - Low stock threshold + suggestions
 * - increaseStock/decreaseStock
 * - adjustQuantity(User, String, int)
 * - seedDefaultStockIfEmpty()
 * - getInstance() singleton support
 */
public class InventoryService {

    // ====== Singleton (some UI code calls getInstance()) ======
    private static InventoryService INSTANCE;

    public static InventoryService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InventoryService(AccessControl.getInstance());
        }
        return INSTANCE;
    }

    // ====== Inner InventoryEvent class ======
    public static final class InventoryEvent {

        public enum Type {
            ADD,
            REMOVE,
            INCREASE,
            DECREASE,
            UPDATE
        }

        private final String productId;
        private final String productName;
        private final String username;
        private final int delta;
        private final int newQuantity;
        private final Type type;
        private final LocalDateTime timestamp;

        public InventoryEvent(String productId,
                              String productName,
                              String username,
                              int delta,
                              int newQuantity,
                              Type type,
                              LocalDateTime timestamp) {

            this.productId = productId;
            this.productName = productName;
            this.username = username;
            this.delta = delta;
            this.newQuantity = newQuantity;
            this.type = type;
            this.timestamp = timestamp;
        }

        public String getProductId()     { return productId; }
        public String getProductName()   { return productName; }
        public String getUsername()      { return username; }
        public int getDelta()            { return delta; }
        public int getNewQuantity()      { return newQuantity; }
        public Type getType()            { return type; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // ====== Fields ======
    private final AccessControl accessControl;
    private final Map<String, Product> products = new HashMap<>();
    private final List<InventoryEvent> history = new ArrayList<>();
    private final Map<String, Integer> restockThresholds = new HashMap<>();

    private static final int DEFAULT_THRESHOLD = 5;

    // ====== Constructor ======
    public InventoryService(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    // ====== CRUD ======

    public boolean addProduct(User actor, Product product) {
        if (!accessControl.canManageStock(actor)) return false;

        products.put(product.getId(), product);
        recordEvent(product.getId(), product.getName(), actor.getUsername(),
                product.getQuantity(), product.getQuantity(), InventoryEvent.Type.ADD);

        return true;
    }

    public boolean removeProduct(User actor, String id) {
        if (!accessControl.canManageStock(actor)) return false;

        Product removed = products.remove(id);
        if (removed == null) return false;

        recordEvent(id, removed.getName(), actor.getUsername(),
                -removed.getQuantity(), 0, InventoryEvent.Type.REMOVE);

        return true;
    }

    public boolean updateProduct(User actor, String id, int qty, double price) {
        if (!accessControl.canManageStock(actor)) return false;

        Product p = products.get(id);
        if (p == null) return false;

        int oldQty = p.getQuantity();
        p.setQuantity(qty);
        p.setPrice(price);

        recordEvent(id, p.getName(), actor.getUsername(),
                qty - oldQty, qty, InventoryEvent.Type.UPDATE);

        return true;
    }

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

    // ====== Search ======
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

    // ====== Stock +/- ======

    public boolean increaseStock(String id) {
        Product p = products.get(id);
        if (p == null) return false;

        p.setQuantity(p.getQuantity() + 1);
        recordEvent(id, p.getName(), null, +1, p.getQuantity(), InventoryEvent.Type.INCREASE);
        return true;
    }

    public boolean decreaseStock(String id) {
        Product p = products.get(id);
        if (p == null || p.getQuantity() <= 0) return false;

        p.setQuantity(p.getQuantity() - 1);
        recordEvent(id, p.getName(), null, -1, p.getQuantity(), InventoryEvent.Type.DECREASE);
        return true;
    }

    /**
     * Used by UI to adjust stock by any positive or negative number.
     */
    public boolean adjustQuantity(User actor, String id, int delta) {
        if (!accessControl.canManageStock(actor)) return false;

        Product p = products.get(id);
        if (p == null) return false;

        int newQty = p.getQuantity() + delta;
        if (newQty < 0) return false;

        p.setQuantity(newQty);

        InventoryEvent.Type type =
                delta > 0 ? InventoryEvent.Type.INCREASE :
                delta < 0 ? InventoryEvent.Type.DECREASE :
                InventoryEvent.Type.UPDATE;

        recordEvent(id, p.getName(), actor.getUsername(), delta, newQty, type);
        return true;
    }

    // ====== Low stock / threshold ======

    public List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();
        for (Product p : products.values()) {
            int th = getRestockThreshold(p.getId());
            if (p.getQuantity() <= th) list.add(p);
        }
        return list;
    }

    public int getRestockThreshold(String productId) {
        return restockThresholds.getOrDefault(productId, DEFAULT_THRESHOLD);
    }

    public int getSuggestedRestockQuantity(Product p) {
        int th = getRestockThreshold(p.getId());
        int target = th * 2;
        return Math.max(0, target - p.getQuantity());
    }

    public void setRestockThreshold(User actor, String productId, int threshold) {
        if (!accessControl.canManageStock(actor)) return;
        restockThresholds.put(productId, Math.max(0, threshold));
    }

    // ====== History ======
    public List<InventoryEvent> getHistory() {
        return new ArrayList<>(history);
    }

    private void recordEvent(String id, String name, String user, int delta,
                             int newQty, InventoryEvent.Type type) {
        history.add(new InventoryEvent(
                id, name, user, delta, newQty, type, LocalDateTime.now()
        ));
    }

    // ====== Default stock population ======

    public void seedDefaultStockIfEmpty() {
        if (!products.isEmpty()) return;

        products.put("A01", new Product("A01", "Apples", 20, 0.50));
        products.put("B01", new Product("B01", "Bananas", 30, 0.40));
        products.put("O01", new Product("O01", "Oranges", 25, 0.60));

        // No user = system event
        recordEvent("A01", "Apples", "system", 20, 20, InventoryEvent.Type.ADD);
        recordEvent("B01", "Bananas", "system", 30, 30, InventoryEvent.Type.ADD);
        recordEvent("O01", "Oranges", "system", 25, 25, InventoryEvent.Type.ADD);
    }
}
