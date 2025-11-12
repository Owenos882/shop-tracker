package com.shoptracker;

import java.util.*;

public class UserRepository {
    private final Map<String, User> byUsername = new HashMap<>();

    public boolean exists(String username) {
        return byUsername.containsKey(username);
    }

    public void save(User user) {
        byUsername.put(user.getUsername(), user);
    }

    public Optional<User> find(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }

    public User findByUsername(String username) {
        return byUsername.get(username);
    }

    public List<User> findAll() {
        return new ArrayList<>(byUsername.values());
    }

    public void delete(String username) {
        byUsername.remove(username);
    }

    public void clear() {
        byUsername.clear();
    }
}
