package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceCreateTest {

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
        boolean created = userService.createUser(admin, alice);

        assertTrue(created);
        assertTrue(repo.exists("alice"));
    }

    @Test
    void cannotCreateDuplicateUser() {
        User firstAlice = new User("alice", "1234", "Alice", "alice1@shop.com", Role.USER);
        User secondAlice = new User("alice", "5678", "Alice 2", "alice2@shop.com", Role.USER);

        assertTrue(userService.createUser(admin, firstAlice));
        assertFalse(userService.createUser(admin, secondAlice));
    }
}
