package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceDeleteTest {

    private UserService userService;
    private UserRepository repo;
    private User admin;

    @BeforeEach
    void setUp() {
        repo = UserRepository.getInstance();
        repo.clear();

        admin = new User("admin", "pass", "Admin User", "admin@shop.com", Role.ADMIN);
        repo.save(admin);
        repo.save(new User("user1", "1111", "User One", "user1@shop.com", Role.USER));

        userService = new UserService(repo, AccessControl.getInstance());
    }

    @Test
    void adminCanDeleteExistingUser() {
        assertTrue(userService.deleteUser(admin, "user1"));
        assertFalse(repo.exists("user1"));
    }

    @Test
    void cannotDeleteNonExistingUser() {
        assertFalse(userService.deleteUser(admin, "does-not-exist"));
    }
}
