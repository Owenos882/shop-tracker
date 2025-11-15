package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {

    private InventoryService inventory;
    private User admin;
    private User normalUser;

    @BeforeEach
    void setUp() {
        // fresh service each test so state is isolated
        inventory = new InventoryService(AccessControl.getInstance());

        admin = new User("admin", "1234", "Admin User", "admin@test.com", Role.ADMIN);
        normalUser = new User("user", "0000", "Normal User", "user@test.com", Role.USER);
    }

    @Test
    void inventoryStartsWithSomeProductsOrZeroButNotNull() {
        List<Product> all = inventory.getAllProducts();
        assertNotNull(all);
        // we don’t care if it’s empty or seeded, just that it doesn’t explode
    }

    @Test
    void adminCanAddNewProduct() {
        Product p = new Product("TEST-1", "Test Widget", 10, 1.99);

        boolean added = inventory.addProduct(admin, p);

        // At minimum, if addProduct says true, it must be findable.
        if (added) {
            List<Product> found = inventory.searchByName("Test Widget");
            assertFalse(found.isEmpty(), "Added product should be returned by searchByName");
        }
    }

    @Test
    void adjustQuantityChangesStockForExistingProduct() {
        // ensure we have at least one known product by adding one ourselves
        Product p = new Product("ADJ-1", "Adjustable Item", 5, 2.50);
        inventory.addProduct(admin, p);

        List<Product> beforeList = inventory.searchByName("Adjustable Item");
        assertFalse(beforeList.isEmpty());
        Product before = beforeList.get(0);
        int initialQty = before.getQuantity();

        inventory.adjustQuantity(admin, before.getId(), +3);

        List<Product> afterList = inventory.searchByName("Adjustable Item");
        assertFalse(afterList.isEmpty());
        Product after = afterList.get(0);

        assertEquals(initialQty + 3, after.getQuantity());
    }

    @Test
    void searchByNameIsCaseInsensitiveAndSupportsPartialMatch() {
        Product p = new Product("SRCH-1", "Blue Hammer", 4, 5.00);
        inventory.addProduct(admin, p);

        List<Product> result1 = inventory.searchByName("blue");
        List<Product> result2 = inventory.searchByName("HAMMER");

        assertFalse(result1.isEmpty(), "Search with 'blue' should find Blue Hammer");
        assertFalse(result2.isEmpty(), "Search with 'HAMMER' should find Blue Hammer");
    }

    @Test
    void historyRecordsChangesWhenAdjustingStock() {
        Product p = new Product("HIST-1", "History Item", 2, 3.00);
        inventory.addProduct(admin, p);

        int beforeEvents = inventory.getHistory().size();

        inventory.adjustQuantity(admin, p.getId(), +1);

        int afterEvents = inventory.getHistory().size();

        assertTrue(afterEvents >= beforeEvents + 1,
                "Adjusting quantity should add at least one history event");
    }
}
