package com.shoptracker;

public class AccessControl {

    public static boolean isAdmin(User u) {
        return u != null && u.getRole() == Role.ADMIN;
    }

    public static boolean isManager(User u) {
        return u != null && u.getRole() == Role.MANAGER;
    }

    /** Admin OR Manager can manage stock (add/remove). */
    public static boolean canManageStock(User u) {
        return isAdmin(u) || isManager(u);
    }

    /** Only Admin can manage users (create/delete/modify roles). */
    public static boolean canManageUsers(User u) {
        return isAdmin(u);
    }
}
