package com.example.e_library.models;

public class Comment {
    private String content;
    private String username;
    private String avatar;
    private String createdAt;

    // Constructor
    public Comment(String content, String username, String avatar, String createdAt) {
        this.content = content;
        this.username = username;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    // Getters
    public String getContent() { return content; }
    public String getUsername() { return username; }
    public String getAvatar() { return avatar; }
    public String getCreatedAt() { return createdAt; }
}