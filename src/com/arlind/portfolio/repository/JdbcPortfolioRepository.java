package com.arlind.portfolio.repository;

import com.arlind.portfolio.model.Message;
import com.arlind.portfolio.model.Profile;
import com.arlind.portfolio.model.Project;
import com.arlind.portfolio.model.Skill;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcPortfolioRepository implements PortfolioRepository {
    private final DatabaseConfig config;

    public JdbcPortfolioRepository(DatabaseConfig config) {
        this.config = config;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "MySQL JDBC driver was not found. Place mysql-connector-j.jar in the lib folder and run the app with that jar in the classpath.",
                    e
            );
        }
    }

    @Override
    public Profile getProfile() {
        String sql = """
                SELECT full_name, title, bio, philosophy, image_url, age, location, email, password
                FROM profile_settings
                WHERE id = 1
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.next()) {
                throw new IllegalStateException("Profile settings were not found in the database.");
            }

            return new Profile(
                    resultSet.getString("full_name"),
                    resultSet.getString("title"),
                    resultSet.getString("bio"),
                    resultSet.getString("philosophy"),
                    resultSet.getString("image_url"),
                    resultSet.getInt("age"),
                    resultSet.getString("location"),
                    resultSet.getString("email"),
                    resultSet.getString("password")
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Loading profile settings failed.", e);
        }
    }

    @Override
    public Profile updateProfile(Profile profile) {
        String sql = """
                UPDATE profile_settings
                SET full_name = ?, title = ?, bio = ?, philosophy = ?, image_url = ?, age = ?, location = ?, email = ?
                WHERE id = 1
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile.getFullName());
            statement.setString(2, profile.getTitle());
            statement.setString(3, profile.getBio());
            statement.setString(4, profile.getPhilosophy());
            statement.setString(5, profile.getImageUrl());
            statement.setInt(6, profile.getAge());
            statement.setString(7, profile.getLocation());
            statement.setString(8, profile.getEmail());
            statement.executeUpdate();
            return getProfile();
        } catch (SQLException e) {
            throw new IllegalStateException("Updating profile settings failed.", e);
        }
    }

    @Override
    public void changePassword(String newPassword) {
        String sql = "UPDATE profile_settings SET password = ? WHERE id = 1";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Changing password failed.", e);
        }
    }

    @Override
    public List<Project> getProjects() {
        String sql = "SELECT id, title, description, stack FROM projects ORDER BY id DESC";
        List<Project> projects = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                projects.add(new Project(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("stack")
                ));
            }

            return projects;
        } catch (SQLException e) {
            throw new IllegalStateException("Loading projects failed.", e);
        }
    }

    @Override
    public Project addProject(String title, String description, String stack) {
        String sql = "INSERT INTO projects (title, description, stack) VALUES (?, ?, ?)";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, stack);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Project(keys.getInt(1), title, description, stack);
                }
            }

            throw new IllegalStateException("Project ID was not generated.");
        } catch (SQLException e) {
            throw new IllegalStateException("Saving the project failed.", e);
        }
    }

    @Override
    public Project updateProject(int id, String title, String description, String stack) {
        String sql = "UPDATE projects SET title = ?, description = ?, stack = ? WHERE id = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, stack);
            statement.setInt(4, id);
            int updatedRows = statement.executeUpdate();

            if (updatedRows == 0) {
                throw new IllegalStateException("Project was not found.");
            }

            return new Project(id, title, description, stack);
        } catch (SQLException e) {
            throw new IllegalStateException("Updating the project failed.", e);
        }
    }

    @Override
    public void deleteProject(int id) {
        String sql = "DELETE FROM projects WHERE id = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Deleting the project failed.", e);
        }
    }

    @Override
    public Message addMessage(String senderName, String email, String body) {
        String sql = """
                INSERT INTO messages (sender_name, email, body, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, senderName);
            statement.setString(2, email);
            statement.setString(3, body);
            statement.executeUpdate();

            int id = 0;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }

            return new Message(id, senderName, email, body, "just-now");
        } catch (SQLException e) {
            throw new IllegalStateException("Saving the message failed.", e);
        }
    }

    @Override
    public List<Message> getMessages() {
        String sql = "SELECT id, sender_name, email, body, created_at FROM messages ORDER BY created_at DESC, id DESC";
        List<Message> messages = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                messages.add(new Message(
                        resultSet.getInt("id"),
                        resultSet.getString("sender_name"),
                        resultSet.getString("email"),
                        resultSet.getString("body"),
                        String.valueOf(resultSet.getTimestamp("created_at"))
                ));
            }

            return messages;
        } catch (SQLException e) {
            throw new IllegalStateException("Loading messages failed.", e);
        }
    }

    @Override
    public void deleteMessage(int id) {
        String sql = "DELETE FROM messages WHERE id = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Deleting the message failed.", e);
        }
    }

    @Override
    public List<Skill> getSkills() {
        String sql = "SELECT id, skill_name, category FROM skills ORDER BY id";
        List<Skill> skills = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                skills.add(new Skill(
                        resultSet.getInt("id"),
                        resultSet.getString("skill_name"),
                        resultSet.getString("category")
                ));
            }

            return skills;
        } catch (SQLException e) {
            throw new IllegalStateException("Loading skills failed.", e);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.username(), config.password());
    }
}
