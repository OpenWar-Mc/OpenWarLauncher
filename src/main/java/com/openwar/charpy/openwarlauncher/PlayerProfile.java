package com.openwar.charpy.openwarlauncher;

public class PlayerProfile {
    private String username;
    private String avatarUrl;
    private String token;
    private String uuid;

    public PlayerProfile(String username, String avatarUrl, String minecraft_token, String uuid) {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.token = minecraft_token;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }
    public String getToken() {return token;}
    public String getUuid() {return uuid;}
    public String getAvatarUrl() {
        return avatarUrl;
    }
}