package com.arlind.portfolio.model;

public class Profile {
    private final String fullName;
    private final String title;
    private final String bio;
    private final String philosophy;
    private final String imageUrl;
    private final int age;
    private final String location;
    private final String email;
    private final String password;

    public Profile(String fullName, String title, String bio, String philosophy, String imageUrl, int age, String location, String email, String password) {
        this.fullName = fullName;
        this.title = title;
        this.bio = bio;
        this.philosophy = philosophy;
        this.imageUrl = imageUrl;
        this.age = age;
        this.location = location;
        this.email = email;
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTitle() {
        return title;
    }

    public String getBio() {
        return bio;
    }

    public String getPhilosophy() {
        return philosophy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getAge() {
        return age;
    }

    public String getLocation() {
        return location;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Profile withPassword(String newPassword) {
        return new Profile(fullName, title, bio, philosophy, imageUrl, age, location, email, newPassword);
    }

    public String toJson() {
        return "{"
                + "\"fullName\":\"" + escape(fullName) + "\","
                + "\"title\":\"" + escape(title) + "\","
                + "\"bio\":\"" + escape(bio) + "\","
                + "\"philosophy\":\"" + escape(philosophy) + "\","
                + "\"imageUrl\":\"" + escape(imageUrl) + "\","
                + "\"age\":" + age + ","
                + "\"location\":\"" + escape(location) + "\","
                + "\"email\":\"" + escape(email) + "\""
                + "}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
