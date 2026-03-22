package com.arlind.portfolio.model;

public class Message {
    private final int id;
    private final String senderName;
    private final String email;
    private final String body;
    private final String createdAt;

    public Message(int id, String senderName, String email, String body, String createdAt) {
        this.id = id;
        this.senderName = senderName;
        this.email = email;
        this.body = body;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getEmail() {
        return email;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"senderName\":\"" + escape(senderName) + "\","
                + "\"email\":\"" + escape(email) + "\","
                + "\"body\":\"" + escape(body) + "\","
                + "\"createdAt\":\"" + escape(createdAt) + "\""
                + "}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
