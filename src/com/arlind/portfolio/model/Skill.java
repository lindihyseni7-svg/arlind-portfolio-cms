package com.arlind.portfolio.model;

public class Skill {
    private final int id;
    private final String skillName;
    private final String category;

    public Skill(int id, String skillName, String category) {
        this.id = id;
        this.skillName = skillName;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getCategory() {
        return category;
    }

    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"skillName\":\"" + escape(skillName) + "\","
                + "\"category\":\"" + escape(category) + "\""
                + "}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
