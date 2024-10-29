package com.openwar.charpy.openwarlauncher;

public class PlayerProfile {
    private String username;
    private String avatarUrl;

    public PlayerProfile(String username, String avatarUrl) {
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}