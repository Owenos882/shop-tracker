package com.shoptracker;

import java.util.Objects;

public class User {
    private final String username;
    private String password;     // For demo only. In real apps, store a hash.
    private String fullName;
    private String email;
    private Role role;
    private boolean active = true;

    public User(String username, String password, String fullName, String email, Role role) {
        this.username = requireNonBlank(username, "username");
        this.password = requireNonBlank(password, "password");
        this.fullName = requireNonBlank(fullName, "fullName");
        this.email = requireNonBlank(email, "email");
        this.role = Objects.requireNonNull(role, "role");
    }

    private static String requireNonBlank(String v, String field) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(field + " is required");
        return v;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }

    public void setPassword(String password) { this.password = requireNonBlank(password, "password"); }
    public void setFullName(String fullName) { this.fullName = requireNonBlank(fullName, "fullName"); }
    public void setEmail(String email) { this.email = requireNonBlank(email, "email"); }
    public void setRole(Role role) { this.role = Objects.requireNonNull(role); }
    public void setActive(boolean active) { this.active = active; }
}
