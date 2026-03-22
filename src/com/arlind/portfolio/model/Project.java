package com.arlind.portfolio.model;

public class Project {
    private final int id;
    private final String title;
    private final String description;
    private final String stack;

    public Project(int id, String title, String description, String stack) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stack = stack;
    }

    public Project(String title, String description, String stack) {
        this(0, title, description, stack);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStack() {
        return stack;
    }

    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"title\":\"" + escape(title) + "\","
                + "\"description\":\"" + escape(description) + "\","
                + "\"stack\":\"" + escape(stack) + "\""
                + "}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
