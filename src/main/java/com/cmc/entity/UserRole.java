package com.cmc.entity;

public enum UserRole {
    ADMIN("Admin"),
    VIP("VIP User"),
    STANDARD("Standard User");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
