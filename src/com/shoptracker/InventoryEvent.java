package com.shoptracker;

import java.time.LocalDateTime;

/**
 * Represents a single change to a product's stock level.
 * Used for history tracking and analytics.
 */
public class InventoryEvent {

    public enum EventType {
        ADD,        // Stock was increased
        REMOVE,     // Stock was decreased
        SET,        // Stock was set to an exact value
        ADJUST      // Stock was adjusted by +/- value
    }

    private final String productId;
    private final String productName;
    private final String username;         // Who performed the action
    private final EventType type;
    private final int oldQuantity;
    private final int newQuantity;
    private final int delta;               // Change amount (+/-)
    private final LocalDateTime timestamp;

    public InventoryEvent(
            String productId,
            String productName,
            String username,
            EventType type,
            int oldQuantity,
            int newQuantity
    ) {
        this.productId = productId;
        this.productName = productName;
        this.username = username;
        this.type = type;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.delta = newQuantity - oldQuantity;
        this.timestamp = LocalDateTime.now();
    }

    // -------- GETTERS --------

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getUsername() {
        return username;
    }

    public EventType getType() {
        return type;
    }

    public int getOldQuantity() {
        return oldQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public int getDelta() {
        return delta;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // For debugging or logging
    @Override
    public String toString() {
        return timestamp + " | " + username + " | " + type +
                " | " + productName + " (" + productId + ")" +
                " | " + oldQuantity + " → " + newQuantity +
                " | Δ " + delta;
    }
}
