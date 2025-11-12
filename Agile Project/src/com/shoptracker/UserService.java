package com.shoptracker;

import java.util.*;
import java.util.stream.Collectors;

public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    // ---------- SCRUM-13: Create user ----------
    public User createUser(User actingAdmin, String username, String password,
                           String fullName, String email, Role role) {
        requireAdmin(actingAdmin, "create users");
        if (repo.exists(username)) throw new IllegalArgumentException("username must be unique");
        User u = new User(username, password, fullName, email, role);
        repo.save(u);
        return u;
    }

    // ---------- SCRUM-15: List & filter ----------
    public List<User> listUsers() {
        return repo.findAll();
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) return listUsers();
        String q = query.toLowerCase();
        return repo.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(q)
                          || u.getFullName().toLowerCase().contains(q)
                          || u.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // ---------- SCRUM-14: Modify permissions/role ----------
    public void changeUserRole(User actingAdmin, String targetUsername, Role newRole) {
        requireAdmin(actingAdmin, "modify permissions");
        User target = repo.find(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("user not found"));
        target.setRole(newRole);
    }

    // ---------- SCRUM-16: Delete user ----------
    public void deleteUser(User actingAdmin, String targetUsername) {
        requireAdmin(actingAdmin, "delete users");
        if (actingAdmin.getUsername().equals(targetUsername)) {
            // Optional restriction from story
            throw new IllegalStateException("admin cannot delete own profile");
        }
        if (!repo.exists(targetUsername)) throw new NoSuchElementException("user not found");
        repo.delete(targetUsername);
    }

    private static void requireAdmin(User acting, String action) {
        if (!AccessControl.canManageUsers(acting)) {
            throw new SecurityException("Only admin can " + action);
        }
    }
}
