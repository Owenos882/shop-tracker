package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceDeleteTest {
    private UserRepository repo;
    private UserService service;
    private User admin, other;

    @BeforeEach
    void setup() {
        repo = new UserRepository();
        service = new UserService(repo);
        admin = new User("admin","pw","Admin","admin@x.com", Role.ADMIN);
        other = new User("kate","pw","Kate","k@x.com", Role.USER);
        repo.save(admin);
        repo.save(other);
    }

    @Test
    void adminCanDeleteOtherUser() {
        service.deleteUser(admin, "kate");
        assertFalse(repo.exists("kate"));
    }

    @Test
    void adminCannotDeleteSelf() {
        assertThrows(IllegalStateException.class, () -> service.deleteUser(admin, "admin"));
    }

    @Test
    void nonAdminCannotDelete() {
        assertThrows(SecurityException.class, () -> service.deleteUser(other, "admin"));
    }
}
