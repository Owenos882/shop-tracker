package com.shoptracker.tests;

import com.shoptracker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        List<User> r1 = service.searchUsers("maria");
        assertEquals(1, r1.size());

        List<User> r2 = service.searchUsers("stone");
        assertEquals(1, r2.size());

        List<User> r3 = service.searchUsers("@x.com");
        assertEquals(3, r3.size());
    }
}
