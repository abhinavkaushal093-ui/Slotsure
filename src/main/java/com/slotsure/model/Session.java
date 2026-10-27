package com.slotsure.model;

public class Session {
    private static long currentUserId = 0;
    private static String currentUserRole = ""; 
    private static String currentUserName = "";

    public static void loginUser(long id, String name, String role) {
        currentUserId = id;
        currentUserName = name;
        currentUserRole = role;
    }

    public static void logout() {
        currentUserId = 0;
        currentUserName = "";
        currentUserRole = "";
    }

    public static long getUserId() { return currentUserId; }
    public static String getRole() { return currentUserRole; }
    public static String getName() { return currentUserName; }
    
    public static boolean isAdmin() {
        return "ADMIN".equals(currentUserRole) || "FACULTY".equals(currentUserRole);
    }
}