package com.shoptracker;

public final class AccessControl {

    private static final AccessControl INSTANCE = new AccessControl();

    private AccessControl() {
        // singleton
    }

    public static AccessControl getInstance() {
        return INSTANCE;
    }

    // shared logic so SonarQube doesn’t see duplicated code
    private boolean hasElevatedRole(User user) {
        return user != null &&
               (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER);
    }

    public boolean canManageStock(User user) {
        return hasElevatedRole(user);
    }

    public boolean canManageUsers(User user) {
        return hasElevatedRole(user);
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public boolean isManager(User user) {
        return user != null && user.getRole() == Role.MANAGER;
    }
}
