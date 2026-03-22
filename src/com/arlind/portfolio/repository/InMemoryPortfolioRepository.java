package com.arlind.portfolio.repository;

import com.arlind.portfolio.model.Profile;
import com.arlind.portfolio.model.Project;
import com.arlind.portfolio.model.Message;
import com.arlind.portfolio.model.Skill;

import java.util.ArrayList;
import java.util.List;

public class InMemoryPortfolioRepository implements PortfolioRepository {
    private Profile profile = new Profile(
            "Arlind Hyseni",
            "Full-Stack Software Engineer",
            "Ndertoj produkte moderne me Java, API te pastra dhe databaza reale MySQL.",
            "Programimi nuk eshte thjesht kodim; eshte arkitektura e se ardhmes. Si student 19-vjecar ne FSHMN dhe zhvillues i apasionuar, une besoj se inovacioni lind aty ku matematika takon imagjinaten. Cdo projekt qe ndertoj eshte nje perpjekje per te thjeshtuar kompleksitetin.",
            "https://ui-avatars.com/api/?name=Arlind+Hyseni&background=0f172a&color=06b6d4&size=280",
            19,
            "Kosove",
            "arlind@example.com",
            "admin123"
    );
    private final List<Project> projects = new ArrayList<>(List.of(
            new Project(
                    1,
                    "Portfolio Personal",
                    "Website-i im i pare me Java si server dhe nje strukture qe mund te lidhet me SQL.",
                    "Java, HTML, CSS, JavaScript"
            ),
            new Project(
                    2,
                    "Menaxhim Studentor",
                    "Ide per aplikacion ku ruhen studentet, lendet dhe notat ne databaze.",
                    "Java, SQL"
            ),
            new Project(
                    3,
                    "Kontakt Form API",
                    "Formular kontakti qe me vone mund te ruaje mesazhet ne databaze.",
                    "Java, REST, SQL"
            )
    ));
    private final List<Message> messages = new ArrayList<>();
    private final List<Skill> skills = new ArrayList<>(List.of(
            new Skill(1, "HTML", "Frontend"),
            new Skill(2, "CSS", "Frontend"),
            new Skill(3, "JavaScript", "Frontend"),
            new Skill(4, "Bootstrap", "Frontend"),
            new Skill(5, "GitHub", "Tools"),
            new Skill(6, "Java", "Backend"),
            new Skill(7, "PHP", "Backend"),
            new Skill(8, "MySQL (Advanced)", "Database")
    ));

    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public Profile updateProfile(Profile profile) {
        this.profile = profile.withPassword(this.profile.getPassword());
        return profile;
    }

    @Override
    public void changePassword(String newPassword) {
        profile = profile.withPassword(newPassword);
    }

    @Override
    public List<Project> getProjects() {
        return List.copyOf(projects);
    }

    @Override
    public Project addProject(String title, String description, String stack) {
        int nextId = projects.stream().mapToInt(Project::getId).max().orElse(0) + 1;
        Project project = new Project(nextId, title, description, stack);
        projects.add(project);
        return project;
    }

    @Override
    public Project updateProject(int id, String title, String description, String stack) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == id) {
                Project updated = new Project(id, title, description, stack);
                projects.set(i, updated);
                return updated;
            }
        }
        throw new IllegalStateException("Project was not found.");
    }

    @Override
    public void deleteProject(int id) {
        projects.removeIf(project -> project.getId() == id);
    }

    @Override
    public Message addMessage(String senderName, String email, String body) {
        Message message = new Message(messages.size() + 1, senderName, email, body, "local-demo");
        messages.add(message);
        return message;
    }

    @Override
    public List<Message> getMessages() {
        return List.copyOf(messages);
    }

    @Override
    public void deleteMessage(int id) {
        messages.removeIf(message -> message.getId() == id);
    }

    @Override
    public List<Skill> getSkills() {
        return List.copyOf(skills);
    }
}
