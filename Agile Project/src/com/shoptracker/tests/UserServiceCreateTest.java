package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceCreateTest {

    private UserRepository repo;
    private UserService service;
    private User admin;

    @BeforeEach
    void setup() {
        repo = new UserRepository();
        service = new UserService(repo);
        admin = new User("admin","pass","Admin","admin@example.com", Role.ADMIN);
        repo.save(admin);
    }

    @Test
    void createsUser_whenValidAndUnique() {
        User u = service.createUser(admin, "alice", "pw", "Alice", "a@x.com", Role.USER);
        assertEquals("alice", u.getUsername());
        assertTrue(repo.exists("alice"));
    }

    @Test
    void rejectsDuplicateUsername() {
        service.createUser(admin, "bob", "pw", "Bob", "b@x.com", Role.USER);
        assertThrows(IllegalArgumentException.class, () ->
            service.createUser(admin, "bob", "pw", "Bob Two", "b2@x.com", Role.USER));
    }

    @Test
    void nonAdminCannotCreate() {
        User user = new User("john","pw","John","j@x.com", Role.USER);
        repo.save(user);
        assertThrows(SecurityException.class, () ->
            service.createUser(user, "kate", "pw", "Kate", "k@x.com", Role.USER));
    }
}
