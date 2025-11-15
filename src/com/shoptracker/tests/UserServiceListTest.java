package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceListTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setup() {
        repo = UserRepository.getInstance();
        repo.clear();

        repo.save(new User("admin", "pw", "Admin", "admin@x.com", Role.ADMIN));
        repo.save(new User("maria", "pw", "Maria Lane", "maria@x.com", Role.MANAGER));
        repo.save(new User("mike", "pw", "Mike Stone", "mike@x.com", Role.USER));

        service = new UserService(repo);
    }

    @Test
    void listsAllUsers() {
        assertEquals(3, service.listUsers().size());
    }

    @Test
    void searchesByUsernameFullNameOrEmail() {
        assertEquals(1, service.searchUsers("maria").size());
        assertEquals(1, service.searchUsers("stone").size());
        assertEquals(3, service.searchUsers("@x.com").size());
    }
}
