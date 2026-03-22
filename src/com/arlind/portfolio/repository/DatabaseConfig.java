package com.arlind.portfolio.repository;

public class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String url() {
        return url;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public static DatabaseConfig fromEnvironment() {
        String url = System.getenv().getOrDefault(
                "PORTFOLIO_DB_URL",
                "jdbc:mysql://localhost:3306/portfolio_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        String username = System.getenv().getOrDefault("PORTFOLIO_DB_USER", "root");
        String password = System.getenv().getOrDefault("PORTFOLIO_DB_PASSWORD", "");
        return new DatabaseConfig(url, username, password);
    }
}
