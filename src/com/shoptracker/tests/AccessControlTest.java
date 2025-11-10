package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccessControlTest {

    @Test
    void adminAndManagerCanManageStock_userCannot() {
        User admin = new User("a","p","A","a@x.com", Role.ADMIN);
        User mgr   = new User("m","p","M","m@x.com", Role.MANAGER);
        User usr   = new User("u","p","U","u@x.com", Role.USER);

        assertTrue(AccessControl.canManageStock(admin));
        assertTrue(AccessControl.canManageStock(mgr));
        assertFalse(AccessControl.canManageStock(usr));
    }

    @Test
    void onlyAdminCanManageUsers() {
        User admin = new User("a","p","A","a@x.com", Role.ADMIN);
        User mgr   = new User("m","p","M","m@x.com", Role.MANAGER);
        assertTrue(AccessControl.canManageUsers(admin));
        assertFalse(AccessControl.canManageUsers(mgr));
    }
}
