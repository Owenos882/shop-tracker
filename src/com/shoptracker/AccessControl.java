package com.shoptracker;

public final class AccessControl {

    private static final AccessControl INSTANCE = new AccessControl();

    private AccessControl() {}

    public static AccessControl getInstance() {
        return INSTANCE;
    }

    public boolean canManageStock(User user) {
        return user != null &&
                (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER);
    }

    public boolean canManageUsers(User user) {
        return user != null &&
                (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER);
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public boolean isManager(User user) {
        return user != null && user.getRole() == Role.MANAGER;
    }
}
