package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {

    private InventoryService inventory;
    private User admin;
    private User regular;

    @BeforeEach
    void setUp() {
        inventory = InventoryService.getInstance();
        inventory.clearInventory();

        admin = new User("admin", "pw", "Admin User", "admin@shop.com", Role.ADMIN);
        regular = new User("user", "pw", "Regular User", "user@shop.com", Role.USER);
    }

    @Test
    void addProductWithPermission() {
        Product p = new Product("P1", "Hammer", 5, 10.0);
        assertTrue(inventory.addProduct(admin, p));
        assertEquals(1, inventory.size());
    }

    @Test
    void addProductWithoutPermissionFails() {
        Product p = new Product("P2", "Wrench", 4, 8.0);
        assertFalse(inventory.addProduct(regular, p));
        assertEquals(0, inventory.size());
    }

    @Test
    void updateProductWhenExists() {
        Product p = new Product("P3", "Drill", 3, 20.0);
        inventory.addProduct(admin, p);

        assertTrue(inventory.updateProduct(admin, "P3", 5, 25.0));
        Product updated = inventory.getProduct("P3");
        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
        assertEquals(25.0, updated.getPrice(), 0.0001);
    }

    @Test
    void removeProduct() {
        Product p = new Product("P4", "Saw", 2, 15.0);
        inventory.addProduct(admin, p);

        assertTrue(inventory.removeProduct(admin, "P4"));
        assertEquals(0, inventory.size());
    }

    @Test
    void searchByNameIsCaseInsensitiveAndPartial() {
        inventory.addProduct(admin, new Product("P5", "Small Screwdriver", 12, 3.5));
        inventory.addProduct(admin, new Product("P6", "Large Screwdriver", 6, 4.5));

        List<Product> result = inventory.searchByName("screw");
        assertEquals(2, result.size());
    }
}
