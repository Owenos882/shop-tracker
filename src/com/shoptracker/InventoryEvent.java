package com.shoptracker;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents one change in product stock history.
 * Fully immutable and SonarQube-clean.
 */
public final class InventoryEvent {

    public enum EventType {
        ADD,
        REMOVE,
        INCREASE,
        DECREASE,
        UPDATE,
        SET,
        ADJUST
    }

    private final String productId;
    private final String productName;
    private final String username;
    private final EventType type;
    private final int oldQuantity;
    private final int newQuantity;
    private final int delta;
    private final LocalDateTime timestamp;

    /**
     * Creates a new immutable event describing a stock change.
     *
     * @param productId    ID of product
     * @param productName  name at event time (avoids future rename issues)
     * @param username     user performing action ("system" allowed)
     * @param type         event type
     * @param oldQuantity  starting quantity
     * @param newQuantity  resulting quantity
     */
    public InventoryEvent(
            String productId,
            String productName,
            String username,
            EventType type,
            int oldQuantity,
            int newQuantity
    ) {
        this.productId = Objects.requireNonNull(productId, "productId");
        this.productName = Objects.requireNonNull(productName, "productName");
        this.username = (username == null || username.isBlank()) ? "system" : username;
        this.type = Objects.requireNonNull(type, "type");
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.delta = newQuantity - oldQuantity;
        this.timestamp = LocalDateTime.now();
    }

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

    @Override
    public String toString() {
        return timestamp +
                " | " + username +
                " | " + type +
                " | " + productName + " (" + productId + ")" +
                " | " + oldQuantity + " → " + newQuantity +
                " | Δ " + delta;
    }
}
