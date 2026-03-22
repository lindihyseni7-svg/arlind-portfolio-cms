package com.arlind.portfolio.repository;

import com.arlind.portfolio.model.Profile;
import com.arlind.portfolio.model.Project;
import com.arlind.portfolio.model.Message;
import com.arlind.portfolio.model.Skill;

import java.util.List;

public interface PortfolioRepository {
    Profile getProfile();
    Profile updateProfile(Profile profile);
    void changePassword(String newPassword);
    List<Project> getProjects();
    Project addProject(String title, String description, String stack);
    Project updateProject(int id, String title, String description, String stack);
    void deleteProject(int id);
    Message addMessage(String senderName, String email, String body);
    List<Message> getMessages();
    void deleteMessage(int id);
    List<Skill> getSkills();
}
