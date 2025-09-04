package com.example.chat.models;

public class User {
    private String uid;
    private String email;
    private String userName;
    private String avatarUrl;

    public User() {}

    public User(String uid, String email, String userName, String avatarUrl) {
        this.uid = uid;
        this.email = email;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getUserName() { return userName; }
    public String getAvatarUrl() { return avatarUrl; }

    public void setUid(String uid) { this.uid = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
