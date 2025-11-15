package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceCreateTest {

    private UserService userService;
    private UserRepository repo;
    private User admin;

    @BeforeEach
    void setUp() {
        repo = UserRepository.getInstance();
        repo.clear();

        admin = new User("admin", "pass", "Admin User", "admin@shop.com", Role.ADMIN);
        repo.save(admin);

        userService = new UserService(repo, AccessControl.getInstance());
    }

    @Test
    void adminCanCreateUser() {
        User alice = new User("alice", "1234", "Alice", "alice@shop.com", Role.USER);
        assertTrue(userService.createUser(admin, alice));
        assertTrue(repo.exists("alice"));
    }

    @Test
    void cannotCreateDuplicateUser() {
        User u1 = new User("alice", "1234", "Alice", "a1@shop.com", Role.USER);
        User u2 = new User("alice", "xxxx", "Alice2", "a2@shop.com", Role.USER);

        assertTrue(userService.createUser(admin, u1));
        assertFalse(userService.createUser(admin, u2));
    }
}
