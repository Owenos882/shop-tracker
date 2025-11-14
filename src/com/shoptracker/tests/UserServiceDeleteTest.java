package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceDeleteTest {

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
        boolean deleted = userService.deleteUser(admin, "user1");
        assertTrue(deleted);
        assertFalse(repo.exists("user1"));
    }

    @Test
    void cannotDeleteNonExistingUser() {
        boolean deleted = userService.deleteUser(admin, "does-not-exist");
        assertFalse(deleted);
    }
}
