package com.example.stablediffusion.api;

public class ChatMessage {
    private String role;
    private String content;

    // Constructor accepting role and content
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Getter and setter methods
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
