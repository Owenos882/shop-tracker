package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class InventoryServiceTest {

    private InventoryService inventory;
    private User admin;
    private User manager;
    private User user;

    @BeforeEach
    void setUp() {
        inventory = new InventoryService(AccessControl.getInstance());
        admin = new User("admin", "1234", "Admin User", "admin@test.com", Role.ADMIN);
        manager = new User("manager", "pw", "Manager User", "manager@test.com", Role.MANAGER);
        user = new User("user", "pw", "Regular User", "user@test.com", Role.USER);
    }

    // ---------------- BASIC STATE ----------------

    @Test
    void inventoryListIsNeverNull() {
        assertNotNull(inventory.getAllProducts());
    }

    @Test
    void clearInventoryRemovesProductsAndHistory() {
        inventory.addProduct(admin, new Product("X1", "Thing", 5, 1.0));
        inventory.adjustQuantity(admin, "X1", 1);

        inventory.clearInventory();

        assertEquals(0, inventory.getAllProducts().size());
        assertEquals(0, inventory.getHistory().size());
    }

    // ---------------- ADD / REMOVE ----------------

    @Test
    void adminCanAddProduct() {
        Product p = new Product("P1", "Apple", 10, 1.00);
        assertTrue(inventory.addProduct(admin, p));
        assertNotNull(inventory.getProduct("P1"));
    }

    @Test
    void managerCanAddProduct() {
        Product p = new Product("P1", "Apple", 10, 1.00);
        assertTrue(inventory.addProduct(manager, p));
    }

    @Test
    void userCannotAddProduct() {
        Product p = new Product("P2", "Banana", 5, 0.50);
        assertFalse(inventory.addProduct(user, p));
        assertNull(inventory.getProduct("P2"));
    }

    @Test
    void adminCanRemoveProduct() {
        Product p = new Product("P3", "RemoveMe", 4, 2.0);
        inventory.addProduct(admin, p);

        assertTrue(inventory.removeProduct(admin, "P3"));
        assertNull(inventory.getProduct("P3"));
    }

    @Test
    void managerCanRemoveProduct() {
        Product p = new Product("P31", "RemoveMeToo", 4, 2.0);
        inventory.addProduct(admin, p);

        assertTrue(inventory.removeProduct(manager, "P31"));
    }

    @Test
    void userCannotRemoveProduct() {
        Product p = new Product("P4", "KeepMe", 4, 2.0);
        inventory.addProduct(admin, p);

        assertFalse(inventory.removeProduct(user, "P4"));
        assertNotNull(inventory.getProduct("P4"));
    }

    @Test
    void removeProductFailsForUnknownId() {
        assertFalse(inventory.removeProduct(admin, "NOPE"));
    }

    // ---------------- UPDATE ----------------

    @Test
    void adminCanUpdateProduct() {
        Product p = new Product("P5", "Widget", 5, 2.5);
        inventory.addProduct(admin, p);

        assertTrue(inventory.updateProduct(admin, "P5", 20, 9.99));

        Product updated = inventory.getProduct("P5");
        assertEquals(20, updated.getQuantity());
        assertEquals(9.99, updated.getPrice());
    }

    @Test
    void updateFailsForUnknownProduct() {
        assertFalse(inventory.updateProduct(admin, "BAD", 10, 1.0));
    }

    @Test
    void userCannotUpdateProduct() {
        Product p = new Product("UP1", "Item", 5, 2.0);
        inventory.addProduct(admin, p);

        assertFalse(inventory.updateProduct(user, "UP1", 10, 9.99));
    }

    // ---------------- SEARCH ----------------

    @Test
    void searchIsCaseInsensitiveAndPartial() {
        inventory.addProduct(admin, new Product("S1", "Blue Hammer", 3, 3.0));

        assertFalse(inventory.searchByName("blue").isEmpty());
        assertFalse(inventory.searchByName("HAMMER").isEmpty());
        assertFalse(inventory.searchByName("lue Ham").isEmpty());
    }

    @Test
    void searchReturnsEmptyForNoMatch() {
        inventory.addProduct(admin, new Product("S2", "Red Wrench", 2, 2.0));
        assertTrue(inventory.searchByName("xyz").isEmpty());
    }

    // ---------------- QUANTITY ADJUSTMENT ----------------
    // USERS ARE ALLOWED

    @Test
    void adjustQuantityIncreasesCorrectly() {
        inventory.addProduct(admin, new Product("A1", "AdjustItem", 5, 1.0));

        assertTrue(inventory.adjustQuantity(admin, "A1", 3));
        assertEquals(8, inventory.getProduct("A1").getQuantity());
    }

    @Test
    void adjustQuantityDecreasesCorrectly() {
        inventory.addProduct(admin, new Product("A2", "AdjustItem", 5, 1.0));

        assertTrue(inventory.adjustQuantity(admin, "A2", -2));
        assertEquals(3, inventory.getProduct("A2").getQuantity());
    }

    @Test
    void adjustQuantityFailsIfResultNegative() {
        inventory.addProduct(admin, new Product("A3", "AdjustItem", 1, 1.0));

        assertFalse(inventory.adjustQuantity(admin, "A3", -5));
        assertEquals(1, inventory.getProduct("A3").getQuantity());
    }

    @Test
    void userCanAdjustQuantity() {
        inventory.addProduct(admin, new Product("A4", "Item", 5, 1.0));

        assertTrue(inventory.adjustQuantity(user, "A4", 2)); // updated rule
        assertEquals(7, inventory.getProduct("A4").getQuantity());
    }

    // ---------------- INCREASE / DECREASE ----------------

    @Test
    void increaseStockWorks() {
        inventory.addProduct(admin, new Product("I1", "Item", 5, 1.0));
        assertTrue(inventory.increaseStock("I1"));
        assertEquals(6, inventory.getProduct("I1").getQuantity());
    }

    @Test
    void decreaseStockStopsAtZero() {
        inventory.addProduct(admin, new Product("D1", "Item", 0, 1.0));
        assertFalse(inventory.decreaseStock("D1"));
    }

    @Test
    void decreaseStockWorks() {
        inventory.addProduct(admin, new Product("D2", "Item", 3, 1.0));
        assertTrue(inventory.decreaseStock("D2"));
        assertEquals(2, inventory.getProduct("D2").getQuantity());
    }

    // ---------------- LOW STOCK ----------------

    @Test
    void lowStockProductsDetectedCorrectly() {
        inventory.addProduct(admin, new Product("L1", "LowItem", 2, 1.0));
        assertFalse(inventory.getLowStockProducts().isEmpty());
    }

    @Test
    void suggestedRestockWorks() {
        Product p = new Product("L2", "Item", 3, 1.0);
        inventory.addProduct(admin, p);

        assertTrue(inventory.getSuggestedRestockQuantity(p) > 0);
    }

    @Test
    void customThresholdOverridesDefault() {
        inventory.addProduct(admin, new Product("T1", "Threshold", 7, 1.0));
        inventory.setRestockThreshold(admin, "T1", 10);

        assertEquals(10, inventory.getRestockThreshold("T1"));
        assertFalse(inventory.getLowStockProducts().isEmpty());
    }

    // ---------------- HISTORY ----------------

    @Test
    void historyIncreasesAfterStockChange() {
        inventory.addProduct(admin, new Product("H1", "History", 2, 1.0));

        int before = inventory.getHistory().size();
        assertTrue(inventory.adjustQuantity(admin, "H1", 1));

        assertTrue(inventory.getHistory().size() > before);
    }

}
