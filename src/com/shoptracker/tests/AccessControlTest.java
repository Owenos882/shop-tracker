package com.shoptracker.tests;

import com.shoptracker.AccessControl;
import com.shoptracker.Role;
import com.shoptracker.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class AccessControlTest {

    @Test
    void singletonInstanceAlwaysSame() {
        AccessControl ac1 = AccessControl.getInstance();
        AccessControl ac2 = AccessControl.getInstance();
        assertSame(ac1, ac2, "AccessControl should be a singleton");
    }

    @Test
    void adminAndManagerCanManageStock() {
        AccessControl ac = AccessControl.getInstance();

        User admin = new User("admin", "pw", "Admin User", "admin@shop.com", Role.ADMIN);
        User manager = new User("manager", "pw", "Manager User", "manager@shop.com", Role.MANAGER);

        assertTrue(ac.canManageStock(admin));
        assertTrue(ac.canManageStock(manager));
    }

    @Test
    void userAndNullCannotManageStock() {
        AccessControl ac = AccessControl.getInstance();

        User user = new User("user", "pw", "Regular User", "user@shop.com", Role.USER);

        assertFalse(ac.canManageStock(user));
        assertFalse(ac.canManageStock(null));
    }

    @Test
    void adminAndManagerCanManageUsers() {
        AccessControl ac = AccessControl.getInstance();

        User admin = new User("admin", "pw", "Admin User", "admin@shop.com", Role.ADMIN);
        User manager = new User("manager", "pw", "Manager User", "manager@shop.com", Role.MANAGER);

        assertTrue(ac.canManageUsers(admin));
        assertTrue(ac.canManageUsers(manager));
    }

    @Test
    void userAndNullCannotManageUsers() {
        AccessControl ac = AccessControl.getInstance();

        User user = new User("user", "pw", "Regular User", "user@shop.com", Role.USER);

        assertFalse(ac.canManageUsers(user));
        assertFalse(ac.canManageUsers(null));
    }

    @Test
    void isAdminChecksCorrectRole() {
        AccessControl ac = AccessControl.getInstance();

        User admin = new User("admin", "pw", "Admin", "a@x.com", Role.ADMIN);
        User manager = new User("mgr", "pw", "Manager", "m@x.com", Role.MANAGER);
        User normal = new User("bob", "pw", "Bob", "b@x.com", Role.USER);

        assertTrue(ac.isAdmin(admin));
        assertFalse(ac.isAdmin(manager));
        assertFalse(ac.isAdmin(normal));
        assertFalse(ac.isAdmin(null));
    }

    @Test
    void isManagerChecksCorrectRole() {
        AccessControl ac = AccessControl.getInstance();

        User admin = new User("admin", "pw", "Admin", "a@x.com", Role.ADMIN);
        User manager = new User("mgr", "pw", "Manager", "m@x.com", Role.MANAGER);
        User normal = new User("bob", "pw", "Bob", "b@x.com", Role.USER);

        assertTrue(ac.isManager(manager));
        assertFalse(ac.isManager(admin));
        assertFalse(ac.isManager(normal));
        assertFalse(ac.isManager(null));
    }
}
