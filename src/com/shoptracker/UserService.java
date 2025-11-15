package com.shoptracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides user management operations:
 * - Create/delete user
 * - Search users
 * - Change roles
 * - Reset password
 * Uses AccessControl for authorization and ActivityLogService for audit logging.
 */
public final class UserService {

    private final UserRepository repo;
    private final AccessControl accessControl;
    private final ActivityLogService logService;
    private static final String MSG_ACCESS_DENIED = "ACCESS DENIED";


    // Constructor used by tests or services
    public UserService(UserRepository repo) {
        this(repo, AccessControl.getInstance());
    }

    // Full constructor
    public UserService(UserRepository repo, AccessControl accessControl) {
        this.repo = repo;
        this.accessControl = accessControl;
        this.logService = ActivityLogService.getInstance();
    }

    // ---------------- CREATE USER ----------------

    public boolean createUser(User actor, User newUser) {
        if (!accessControl.canManageUsers(actor)) {
            logService.log(MSG_ACCESS_DENIED + safe(actor) + " tried to create user " + newUser.getUsername());
            return false;
        }

        if (repo.exists(newUser.getUsername())) {
            logService.log("User creation FAILED (duplicate username): " + newUser.getUsername());
            return false;
        }

        repo.save(newUser);
        logService.log("User created: " + newUser.getUsername() + " by " + safe(actor));
        return true;
    }

    // ---------------- DELETE USER ----------------

    public boolean deleteUser(User actor, String username) {
        if (!accessControl.canManageUsers(actor)) {
            logService.log(MSG_ACCESS_DENIED + safe(actor) + " tried to delete user " + username);
            return false;
        }

        if (!repo.exists(username)) {
            logService.log("User deletion FAILED (not found): " + username);
            return false;
        }

        repo.delete(username);
        logService.log("User deleted: " + username + " by " + safe(actor));
        return true;
    }

    // ---------------- LIST USERS ----------------

    public List<User> listUsers() {
        return repo.findAll();
    }

    // ---------------- SEARCH ----------------

    public List<User> searchUsers(String query) {
        String q = query.toLowerCase();
        List<User> out = new ArrayList<>();

        for (User u : repo.findAll()) {
            if (u.getUsername().toLowerCase().contains(q)
                    || u.getFullName().toLowerCase().contains(q)
                    || u.getEmail().toLowerCase().contains(q)) {
                out.add(u);
            }
        }
        return out;
    }

    // ---------------- CHANGE ROLE ----------------

    public void changeUserRole(User actor, String targetUsername, Role newRole) {
        if (!accessControl.canManageUsers(actor)) {
            logService.log(MSG_ACCESS_DENIED + safe(actor) +
                    " tried to change role for " + targetUsername);
            throw new SecurityException("User does not have permission to change roles.");
        }

        User target = findOrFail(targetUsername);
        Role old = target.getRole();
        target.setRole(newRole);
        repo.save(target);

        logService.log("Role changed for " + targetUsername + ": " + old + " -> " + newRole +
                " by " + safe(actor));
    }

    // ---------------- RESET PASSWORD ----------------

    public String resetPassword(String username, String email) {
        Optional<User> found = repo.find(username);
        if (found.isEmpty()) {
            logService.log("Password reset FAILED: user not found: " + username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        User user = found.get();
        if (!user.getEmail().equalsIgnoreCase(email)) {
            logService.log("Password reset FAILED for " + username + " (email mismatch)");
            throw new SecurityException("Email does not match stored email.");
        }

        String temp = username + "1234"; // deterministic temp password
        user.setPassword(temp);
        repo.save(user);

        logService.log("Password reset for " + username);
        return temp;
    }

    // ---------------- Helpers ----------------

    private User findOrFail(String username) {
        return repo.find(username)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found: " + username));
    }

    private String safe(User user) {
        return (user == null ? "<null>" : user.getUsername());
    }
}
