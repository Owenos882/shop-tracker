package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServicePermissionTest {

    private UserRepository repo;
    private UserService service;
    private User admin;
    private User user;

    @BeforeEach
    void setup() {
        repo = UserRepository.getInstance();
        repo.clear();

        admin = new User("admin", "pw", "Admin", "admin@x.com", Role.ADMIN);
        user = new User("user", "pw", "User", "user@x.com", Role.USER);

        repo.save(admin);
        repo.save(user);

        service = new UserService(repo, AccessControl.getInstance());
    }

    @Test
    void adminCanChangeRole() {
        service.changeUserRole(admin, "user", Role.MANAGER);
        assertEquals(Role.MANAGER, repo.find("user").get().getRole());
    }

    @Test
    void nonAdminCannotChangeRole() {
        assertThrows(SecurityException.class,
                () -> service.changeUserRole(user, "admin", Role.USER));
    }
}