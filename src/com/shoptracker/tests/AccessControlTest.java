package com.shoptracker.tests;

import com.shoptracker.AccessControl;
import com.shoptracker.Role;
import com.shoptracker.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccessControlTest {

    @Test
    void adminAndManagerCanManageStockButUserCannot() {
        AccessControl ac = AccessControl.getInstance();

        User admin = new User("admin", "pw", "Admin User", "admin@shop.com", Role.ADMIN);
        User manager = new User("manager", "pw", "Manager User", "manager@shop.com", Role.MANAGER);
        User user = new User("user", "pw", "Regular User", "user@shop.com", Role.USER);

        assertTrue(ac.canManageStock(admin));
        assertTrue(ac.canManageStock(manager));
        assertFalse(ac.canManageStock(user));
    }

    @Test
    void adminAndManagerCanManageUsersButUserCannot() {
        AccessControl ac = AccessControl.getInstance();

        User admin = new User("admin", "pw", "Admin User", "admin@shop.com", Role.ADMIN);
        User manager = new User("manager", "pw", "Manager User", "manager@shop.com", Role.MANAGER);
        User user = new User("user", "pw", "Regular User", "user@shop.com", Role.USER);

        assertTrue(ac.canManageUsers(admin));
        assertTrue(ac.canManageUsers(manager));
        assertFalse(ac.canManageUsers(user));
    }
}
