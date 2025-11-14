package com.shoptracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository repo;
    private final AccessControl accessControl;
    private final ActivityLogService logService;

    // Constructor used in some tests / services
    public UserService(UserRepository repo) {
        this(repo, AccessControl.getInstance());
    }

    // Constructor used where explicit AccessControl is passed
    public UserService(UserRepository repo, AccessControl accessControl) {
        this.repo = repo;
        this.accessControl = accessControl;
        this.logService = ActivityLogService.getInstance();
    }

    /**
     * Create a new user.
     * Only ADMIN or MANAGER (via canManageUsers) may create users.
     * Returns true if created, false if duplicate or no permission.
     */
    public boolean createUser(User actor, User newUser) {
        if (!accessControl.canManageUsers(actor)) {
            logService.log("ACCESS DENIED: " + safeUser(actor) + " tried to create user " + newUser.getUsername());
            return false;
        }
        if (repo.exists(newUser.getUsername())) {
            logService.log("User creation FAILED, duplicate username: " + newUser.getUsername());
            return false;
        }
        repo.save(newUser);
        logService.log("User created: " + newUser.getUsername() + " by " + safeUser(actor));
        return true;
    }

    /**
     * Delete an existing user by username.
     * Only ADMIN or MANAGER (via canManageUsers) may delete users.
     * Returns true if deleted, false if user not found or no permission.
     */
    public boolean deleteUser(User actor, String username) {
        if (!accessControl.canManageUsers(actor)) {
            logService.log("ACCESS DENIED: " + safeUser(actor) + " tried to delete user " + username);
            return false;
        }
        if (!repo.exists(username)) {
            logService.log("User deletion FAILED, user not found: " + username);
            return false;
        }
        repo.delete(username);
        logService.log("User deleted: " + username + " by " + safeUser(actor));
        return true;
    }

    /**
     * List all users.
     */
    public List<User> listUsers() {
        return repo.findAll();
    }

    /**
     * Search users by username, full name, or email (case-insensitive, partial match).
     */
    public List<User> searchUsers(String query) {
        String lower = query.toLowerCase();
        List<User> result = new ArrayList<>();
        for (User u : repo.findAll()) {
            if (u.getUsername().toLowerCase().contains(lower)
                    || u.getFullName().toLowerCase().contains(lower)
                    || u.getEmail().toLowerCase().contains(lower)) {
                result.add(u);
            }
        }
        return result;
    }

    /**
     * Change another user's role.
     * Only ADMIN or MANAGER may change roles.
     * Throws SecurityException if no permission.
     * Throws IllegalArgumentException if target user not found.
     */
    public void changeUserRole(User actor, String targetUsername, Role newRole) {
        if (!accessControl.canManageUsers(actor)) {
            logService.log("ACCESS DENIED: " + safeUser(actor) + " tried to change role of " + targetUsername);
            throw new SecurityException("User does not have permission to change roles.");
        }

        User target = findUserOrFail(targetUsername);
        Role oldRole = target.getRole();
        target.setRole(newRole);
        repo.save(target);

        logService.log("Role changed for user " + targetUsername + ": " + oldRole + " -> " + newRole
                + " by " + safeUser(actor));
    }

    /**
     * Forgot password / reset password flow.
     * Validates username + email, then generates a temporary password.
     * Returns the new password so the UI can show it once.
     *
     * Throws IllegalArgumentException if user not found.
     * Throws SecurityException if email does not match.
     */
    public String resetPassword(String username, String email) {
        Optional<User> found = repo.find(username);
        if (found.isEmpty()) {
            logService.log("Password reset FAILED: user not found: " + username);
            throw new IllegalArgumentException("User not found: " + username);
        }

        User user = found.get();
        if (!user.getEmail().equalsIgnoreCase(email)) {
            logService.log("Password reset FAILED for " + username + ": email mismatch");
            throw new SecurityException("Email does not match stored email.");
        }

        String newPassword = generateTemporaryPassword(username);
        user.setPassword(newPassword);
        repo.save(user);

        logService.log("Password reset for user " + username);
        return newPassword;
    }

    // ---- Helpers ----

    private User findUserOrFail(String username) {
        Optional<User> found = repo.find(username);
        if (found.isPresent()) {
            return found.get();
        }
        throw new IllegalArgumentException("Target user not found: " + username);
    }

    private String generateTemporaryPassword(String username) {
        // Simple deterministic temp password for this project
        return username + "1234";
    }

    private String safeUser(User user) {
        return (user == null) ? "<null>" : user.getUsername();
    }
}
