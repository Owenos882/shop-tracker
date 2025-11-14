package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceForgotPasswordTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setUp() {
        repo = UserRepository.getInstance();
        repo.clear();

        User u = new User("alice", "oldpw", "Alice", "alice@shop.com", Role.USER);
        repo.save(u);

        service = new UserService(repo, AccessControl.getInstance());
    }

    @Test
    void resetPasswordSuccess() {
        String newPw = service.resetPassword("alice", "alice@shop.com");
        assertNotNull(newPw);
        assertEquals(newPw, repo.find("alice").get().getPassword());
    }

    @Test
    void resetPasswordFailsForUnknownUser() {
        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("bob", "bob@shop.com"));
    }

    @Test
    void resetPasswordFailsForEmailMismatch() {
        assertThrows(SecurityException.class,
                () -> service.resetPassword("alice", "wrong@shop.com"));
    }
}
