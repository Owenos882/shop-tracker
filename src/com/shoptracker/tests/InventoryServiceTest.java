package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class InventoryServiceTest {
    private InventoryService inventory;
    private User admin;
    private User regular;

 
    @BeforeEach
    void setUp() {
        AccessControl ac = new AccessControl();
        inventory = new InventoryService(ac);
        inventory.clearInventory(); // 🧹 reset static inventory for clean test

        admin = new User("admin", "pass", "Alice Admin", "admin@shop.com", Role.ADMIN);
        regular = new User("user", "1234", "Uma User", "user@shop.com", Role.USER);
    }


    @Test
    void testAddProductWithPermission() {
        Product p = new Product("P1", "Hammer", 5, 10.0);
        assertTrue(inventory.addProduct(admin, p));
        assertEquals(1, inventory.size());
    }

    @Test
    void testAddProductWithoutPermission() {
        Product p = new Product("P2", "Wrench", 4, 8.0);
        assertFalse(inventory.addProduct(regular, p));
        assertEquals(0, inventory.size());
    }

    @Test
    void testUpdateProduct() {
        Product p = new Product("P3", "Drill", 3, 20.0);
        inventory.addProduct(admin, p);
        assertTrue(inventory.updateProduct(admin, "P3", 5, 25.0));
        assertEquals(5, inventory.getProduct("P3").getQuantity());
    }

    @Test
    void testRemoveProduct() {
        Product p = new Product("P4", "Saw", 2, 15.0);
        inventory.addProduct(admin, p);
        assertTrue(inventory.removeProduct(admin, "P4"));
        assertEquals(0, inventory.size());
    }

    @Test
    void testSearchByName() {
        inventory.addProduct(admin, new Product("P5", "Small Screwdriver", 12, 3.5));
        inventory.addProduct(admin, new Product("P6", "Large Screwdriver", 6, 4.5));
        List<Product> result = inventory.searchByName("screw");
        assertEquals(2, result.size());
    }
}
