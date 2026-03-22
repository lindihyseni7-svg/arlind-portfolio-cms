package com.arlind.portfolio.http;

import com.arlind.portfolio.model.Message;
import com.arlind.portfolio.model.Profile;
import com.arlind.portfolio.model.Project;
import com.arlind.portfolio.model.Skill;
import com.arlind.portfolio.repository.PortfolioRepository;
import com.arlind.portfolio.security.PasswordHasher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PortfolioServer {
    private static final String ADMIN_USERNAME = "admin";
    private static final String SESSION_COOKIE = "admin_session=valid";
    private static final String CSRF_COOKIE_NAME = "csrf_token";
    private static final File UPLOADS_DIR = new File("uploads");

    private final int port;
    private final PortfolioRepository repository;
    private volatile boolean running;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public PortfolioServer(int port, PortfolioRepository repository) {
        this.port = port;
        this.repository = repository;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new java.net.InetSocketAddress("0.0.0.0", port));
        running = true;
        serverThread = new Thread(this::acceptLoop, "portfolio-server");
        serverThread.start();
    }

    public int getPort() {
        return serverSocket == null ? port : serverSocket.getLocalPort();
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                handleClient(client);
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleClient(Socket client) {
        try (client;
             BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             OutputStream outputStream = client.getOutputStream()) {

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                writeResponse(outputStream, 400, "text/plain; charset=utf-8", "Bad request".getBytes(StandardCharsets.UTF_8), null);
                return;
            }

            String method = parts[0];
            String path = parts[1];
            Map<String, String> headers = new HashMap<>();

            while (true) {
                String headerLine = reader.readLine();
                if (headerLine == null || headerLine.isBlank()) {
                    break;
                }

                int separatorIndex = headerLine.indexOf(':');
                if (separatorIndex > 0) {
                    headers.put(
                            headerLine.substring(0, separatorIndex).trim().toLowerCase(),
                            headerLine.substring(separatorIndex + 1).trim()
                    );
                }
            }

            String body = readBody(reader, headers);

            try {
                switch (method.toUpperCase()) {
                    case "GET" -> routeGet(path, headers, outputStream);
                    case "POST" -> routePost(path, headers, body, outputStream);
                    case "PUT" -> routePut(path, headers, body, outputStream);
                    case "DELETE" -> routeDelete(path, headers, outputStream);
                    default -> writeJson(outputStream, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (IOException e) {
                String message = e.getMessage() != null && e.getMessage().contains("CSRF")
                        ? "{\"error\":\"CSRF token verification failed.\"}"
                        : "{\"error\":\"Request handling failed.\"}";
                writeResponse(outputStream, 400, "application/json; charset=utf-8", message.getBytes(StandardCharsets.UTF_8), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void routeGet(String path, Map<String, String> headers, OutputStream outputStream) throws IOException {
        String csrfCookie = ensureCsrfCookie(headers);

        if ("/".equals(path)) {
            writeHtml(outputStream, readResource("/static/index.html"), csrfCookie);
            return;
        }
        if ("/cv".equals(path)) {
            writeAttachment(
                    outputStream,
                    "text/plain; charset=utf-8",
                    readResource("/static/Arlind-Hyseni-CV.txt").getBytes(StandardCharsets.UTF_8),
                    "Arlind-Hyseni-CV.txt"
            );
            return;
        }
        if ("/login".equals(path)) {
            writeHtml(outputStream, readResource("/static/login.html"), csrfCookie);
            return;
        }
        if ("/admin".equals(path)) {
            if (!isAuthenticated(headers)) {
                writeRedirect(outputStream, "/login");
                return;
            }
            writeHtml(outputStream, readResource("/static/admin.html"), csrfCookie);
            return;
        }
        if (path.startsWith("/uploads/")) {
            writeUpload(outputStream, path);
            return;
        }
        if ("/api/csrf-token".equals(path)) {
            writeJson(outputStream, 200, "{\"token\":\"" + csrfTokenFromHeaders(headers, csrfCookie) + "\"}", csrfCookie);
            return;
        }
        if ("/robots.txt".equals(path)) {
            String baseUrl = requestBaseUrl(headers);
            String body = "User-agent: *\nAllow: /\nSitemap: " + baseUrl + "/sitemap.xml\n";
            writeResponse(outputStream, 200, "text/plain; charset=utf-8", body.getBytes(StandardCharsets.UTF_8), null);
            return;
        }
        if ("/sitemap.xml".equals(path)) {
            String baseUrl = requestBaseUrl(headers);
            String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
                    + "  <url><loc>" + baseUrl + "/</loc></url>\n"
                    + "  <url><loc>" + baseUrl + "/cv</loc></url>\n"
                    + "</urlset>\n";
            writeResponse(outputStream, 200, "application/xml; charset=utf-8", body.getBytes(StandardCharsets.UTF_8), null);
            return;
        }
        if ("/api/profile".equals(path)) {
            writeJson(outputStream, 200, repository.getProfile().toJson());
            return;
        }
        if ("/api/skills".equals(path)) {
            writeJson(outputStream, 200, skillsJson(repository.getSkills()));
            return;
        }
        if ("/api/projects".equals(path)) {
            writeJson(outputStream, 200, projectsJson(repository.getProjects()));
            return;
        }
        if ("/api/messages".equals(path)) {
            if (!isAuthenticated(headers)) {
                writeJson(outputStream, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            writeJson(outputStream, 200, messagesJson(repository.getMessages()));
            return;
        }

        writeResponse(outputStream, 404, "text/plain; charset=utf-8", "404 - File not found".getBytes(StandardCharsets.UTF_8), null);
    }

    private void routePost(String path, Map<String, String> headers, String body, OutputStream outputStream) throws IOException {
        if ("/login".equals(path)) {
            handleLogin(headers, body, outputStream);
            return;
        }
        if ("/logout".equals(path)) {
            handleLogout(headers, body, outputStream);
            return;
        }
        if ("/api/contact".equals(path)) {
            Map<String, String> formData = parseFormData(body);
            verifyCsrf(headers, formData, null);
            createMessage(formData, outputStream);
            return;
        }

        if (!isAuthenticated(headers)) {
            writeJson(outputStream, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        Map<String, String> formData = parseFormData(body);
        verifyCsrf(headers, formData, null);

        if ("/api/projects".equals(path)) {
            createProject(formData, outputStream);
            return;
        }
        if ("/api/settings".equals(path)) {
            updateSettings(formData, outputStream);
            return;
        }
        if ("/api/change-password".equals(path)) {
            changePassword(formData, outputStream);
            return;
        }
        if ("/api/upload-image".equals(path)) {
            uploadImage(formData, outputStream);
            return;
        }

        writeJson(outputStream, 404, "{\"error\":\"Route not found\"}");
    }

    private void routePut(String path, Map<String, String> headers, String body, OutputStream outputStream) throws IOException {
        if (!isAuthenticated(headers)) {
            writeJson(outputStream, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        Map<String, String> formData = parseFormData(body);
        verifyCsrf(headers, formData, null);

        Integer projectId = extractId(path, "/api/projects/");
        if (projectId != null) {
            String title = formData.getOrDefault("title", "").trim();
            String description = formData.getOrDefault("description", "").trim();
            String stack = formData.getOrDefault("stack", "").trim();

            if (title.isBlank() || description.isBlank() || stack.isBlank()) {
                writeJson(outputStream, 400, "{\"error\":\"Titulli, pershkrimi dhe teknologjite jane te detyrueshme.\"}");
                return;
            }

            writeJson(outputStream, 200, repository.updateProject(projectId, title, description, stack).toJson());
            return;
        }

        writeJson(outputStream, 404, "{\"error\":\"Route not found\"}");
    }

    private void routeDelete(String path, Map<String, String> headers, OutputStream outputStream) throws IOException {
        if (!isAuthenticated(headers)) {
            writeJson(outputStream, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        verifyCsrf(headers, null, headers.get("x-csrf-token"));

        Integer projectId = extractId(path, "/api/projects/");
        if (projectId != null) {
            repository.deleteProject(projectId);
            writeJson(outputStream, 200, "{\"success\":true}");
            return;
        }

        Integer messageId = extractId(path, "/api/messages/");
        if (messageId != null) {
            repository.deleteMessage(messageId);
            writeJson(outputStream, 200, "{\"success\":true}");
            return;
        }

        writeJson(outputStream, 404, "{\"error\":\"Route not found\"}");
    }

    private void handleLogin(Map<String, String> headers, String body, OutputStream outputStream) throws IOException {
        Map<String, String> formData = parseFormData(body);
        verifyCsrf(headers, formData, null);

        String username = formData.getOrDefault("username", "");
        String password = formData.getOrDefault("password", "");
        Profile profile = repository.getProfile();
        String csrfToken = csrfTokenFromHeaders(headers, ensureCsrfCookie(headers));

        if (ADMIN_USERNAME.equals(username) && PasswordHasher.matches(profile.getPassword(), password)) {
            writeRedirect(outputStream, "/admin", SESSION_COOKIE + "; Path=/; HttpOnly\r\nSet-Cookie: " + CSRF_COOKIE_NAME + "=" + csrfToken + "; Path=/");
            return;
        }

        writeHtml(outputStream, readResource("/static/login.html").replace("{{ERROR}}", "Kredencialet jane gabim."), null);
    }

    private void handleLogout(Map<String, String> headers, String body, OutputStream outputStream) throws IOException {
        verifyCsrf(headers, parseFormData(body), null);
        String expiredCookies = "admin_session=; Path=/; Max-Age=0; HttpOnly\r\nSet-Cookie: " + CSRF_COOKIE_NAME + "=; Path=/; Max-Age=0";
        writeRedirect(outputStream, "/login", expiredCookies);
    }

    private void createProject(Map<String, String> formData, OutputStream outputStream) throws IOException {
        String title = formData.getOrDefault("title", "").trim();
        String description = formData.getOrDefault("description", "").trim();
        String stack = formData.getOrDefault("stack", "").trim();

        if (title.isBlank() || description.isBlank() || stack.isBlank()) {
            writeJson(outputStream, 400, "{\"error\":\"Titulli, pershkrimi dhe teknologjite jane te detyrueshme.\"}");
            return;
        }

        writeJson(outputStream, 201, repository.addProject(title, description, stack).toJson());
    }

    private void createMessage(Map<String, String> formData, OutputStream outputStream) throws IOException {
        String senderName = formData.getOrDefault("name", "").trim();
        String email = formData.getOrDefault("email", "").trim();
        String messageBody = formData.getOrDefault("message", "").trim();

        if (senderName.isBlank() || email.isBlank() || messageBody.isBlank()) {
            writeJson(outputStream, 400, "{\"error\":\"Emri, email dhe mesazhi jane te detyrueshme.\"}");
            return;
        }

        writeJson(outputStream, 201, repository.addMessage(senderName, email, messageBody).toJson());
    }

    private void updateSettings(Map<String, String> formData, OutputStream outputStream) throws IOException {
        String fullName = formData.getOrDefault("fullName", "").trim();
        String title = formData.getOrDefault("title", "").trim();
        String bio = formData.getOrDefault("bio", "").trim();
        String philosophy = formData.getOrDefault("philosophy", "").trim();
        String imageUrl = formData.getOrDefault("imageUrl", "").trim();
        String location = formData.getOrDefault("location", "").trim();
        String email = formData.getOrDefault("email", "").trim();
        int age = Integer.parseInt(formData.getOrDefault("age", "0").trim());

        if (fullName.isBlank() || title.isBlank() || bio.isBlank() || philosophy.isBlank() || imageUrl.isBlank() || location.isBlank() || email.isBlank() || age <= 0) {
            writeJson(outputStream, 400, "{\"error\":\"Te gjitha fushat e profilit jane te detyrueshme.\"}");
            return;
        }

        Profile updatedProfile = new Profile(fullName, title, bio, philosophy, imageUrl, age, location, email, repository.getProfile().getPassword());
        writeJson(outputStream, 200, repository.updateProfile(updatedProfile).toJson());
    }

    private void changePassword(Map<String, String> formData, OutputStream outputStream) throws IOException {
        String currentPassword = formData.getOrDefault("currentPassword", "").trim();
        String newPassword = formData.getOrDefault("newPassword", "").trim();
        String confirmPassword = formData.getOrDefault("confirmPassword", "").trim();

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            writeJson(outputStream, 400, "{\"error\":\"Te gjitha fushat e password-it jane te detyrueshme.\"}");
            return;
        }
        if (!PasswordHasher.matches(repository.getProfile().getPassword(), currentPassword)) {
            writeJson(outputStream, 400, "{\"error\":\"Password-i aktual nuk eshte i sakte.\"}");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            writeJson(outputStream, 400, "{\"error\":\"Password-et e reja nuk perputhen.\"}");
            return;
        }
        repository.changePassword(PasswordHasher.hash(newPassword));
        writeJson(outputStream, 200, "{\"success\":true}");
    }

    private void uploadImage(Map<String, String> formData, OutputStream outputStream) throws IOException {
        String imageName = formData.getOrDefault("imageName", "").trim();
        String imageBase64 = formData.getOrDefault("imageData", "").trim();

        if (imageName.isBlank() || imageBase64.isBlank()) {
            writeJson(outputStream, 400, "{\"error\":\"Image upload data is required.\"}");
            return;
        }

        if (!UPLOADS_DIR.exists() && !UPLOADS_DIR.mkdirs()) {
            writeJson(outputStream, 500, "{\"error\":\"Creating uploads directory failed.\"}");
            return;
        }

        String safeName = imageName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String extension = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf('.')) : ".png";
        String fileName = "profile-" + System.currentTimeMillis() + extension;
        byte[] bytes = Base64.getDecoder().decode(imageBase64);
        File file = new File(UPLOADS_DIR, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }

        writeJson(outputStream, 200, "{\"imageUrl\":\"/uploads/" + fileName + "\"}");
    }

    private boolean isAuthenticated(Map<String, String> headers) {
        String cookieHeader = headers.getOrDefault("cookie", "");
        if (cookieHeader.contains(SESSION_COOKIE)) {
            return true;
        }

        String authHeader = headers.get("authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 2);
            return parts.length == 2
                    && ADMIN_USERNAME.equals(parts[0])
                    && PasswordHasher.matches(repository.getProfile().getPassword(), parts[1]);
        }

        return false;
    }

    private Integer extractId(String path, String prefix) {
        if (!path.startsWith(prefix)) {
            return null;
        }

        try {
            return Integer.parseInt(path.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void verifyCsrf(Map<String, String> headers, Map<String, String> formData, String headerToken) throws IOException {
        String cookieToken = getCookieValue(headers, CSRF_COOKIE_NAME);
        String requestToken = headerToken;

        if ((requestToken == null || requestToken.isBlank()) && formData != null) {
            requestToken = formData.get("csrf_token");
        }

        if (cookieToken == null || cookieToken.isBlank() || requestToken == null || !cookieToken.equals(requestToken)) {
            throw new IOException("CSRF token verification failed.");
        }
    }

    private String ensureCsrfCookie(Map<String, String> headers) {
        String cookieToken = getCookieValue(headers, CSRF_COOKIE_NAME);
        if (cookieToken != null && !cookieToken.isBlank()) {
            return null;
        }
        return CSRF_COOKIE_NAME + "=" + UUID.randomUUID() + "; Path=/";
    }

    private String csrfTokenFromHeaders(Map<String, String> headers, String fallbackCookie) {
        String cookieToken = getCookieValue(headers, CSRF_COOKIE_NAME);
        if (cookieToken != null && !cookieToken.isBlank()) {
            return cookieToken;
        }
        if (fallbackCookie == null) {
            return "";
        }
        int equalsIndex = fallbackCookie.indexOf('=');
        int semicolonIndex = fallbackCookie.indexOf(';');
        if (equalsIndex == -1) {
            return "";
        }
        return semicolonIndex == -1
                ? fallbackCookie.substring(equalsIndex + 1)
                : fallbackCookie.substring(equalsIndex + 1, semicolonIndex);
    }

    private String getCookieValue(Map<String, String> headers, String cookieName) {
        String cookieHeader = headers.getOrDefault("cookie", "");
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String trimmed = cookie.trim();
            if (trimmed.startsWith(cookieName + "=")) {
                return trimmed.substring((cookieName + "=").length());
            }
        }
        return null;
    }

    private String requestBaseUrl(Map<String, String> headers) {
        String host = headers.getOrDefault("host", "localhost:" + getPort());
        String protocol = headers.getOrDefault("x-forwarded-proto", "http");
        return protocol + "://" + host;
    }

    private String readBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        int contentLength = headers.containsKey("content-length") ? Integer.parseInt(headers.get("content-length")) : 0;
        if (contentLength <= 0) {
            return "";
        }

        char[] buffer = new char[contentLength];
        int read = 0;
        while (read < contentLength) {
            int result = reader.read(buffer, read, contentLength - read);
            if (result == -1) {
                break;
            }
            read += result;
        }
        return new String(buffer, 0, read);
    }

    private Map<String, String> parseFormData(String body) throws IOException {
        Map<String, String> values = new HashMap<>();
        if (body == null || body.isBlank()) {
            return values;
        }

        for (String pair : body.split("&")) {
            String[] entry = pair.split("=", 2);
            String key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8);
            String value = entry.length > 1 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    private String readResource(String resourcePath) throws IOException {
        try (InputStream inputStream = PortfolioServer.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Missing resource: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).replace("{{ERROR}}", "");
        }
    }

    private void writeUpload(OutputStream outputStream, String path) throws IOException {
        String fileName = path.substring("/uploads/".length());
        File file = new File(UPLOADS_DIR, fileName);
        if (!file.exists() || !file.isFile()) {
            writeResponse(outputStream, 404, "text/plain; charset=utf-8", "404 - File not found".getBytes(StandardCharsets.UTF_8), null);
            return;
        }

        String contentType = "image/png";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (lower.endsWith(".webp")) {
            contentType = "image/webp";
        } else if (lower.endsWith(".gif")) {
            contentType = "image/gif";
        }

        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        writeResponse(outputStream, 200, contentType, bytes, null);
    }

    private String projectsJson(List<Project> projects) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < projects.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(projects.get(i).toJson());
        }
        return json.append("]").toString();
    }

    private String messagesJson(List<Message> messages) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(messages.get(i).toJson());
        }
        return json.append("]").toString();
    }

    private String skillsJson(List<Skill> skills) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < skills.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(skills.get(i).toJson());
        }
        return json.append("]").toString();
    }

    private void writeHtml(OutputStream outputStream, String html, String setCookieHeader) throws IOException {
        String extraHeaders = setCookieHeader == null ? null : "Set-Cookie: " + setCookieHeader + "\r\n";
        writeResponse(outputStream, 200, "text/html; charset=utf-8", html.getBytes(StandardCharsets.UTF_8), extraHeaders);
    }

    private void writeJson(OutputStream outputStream, int statusCode, String json) throws IOException {
        writeJson(outputStream, statusCode, json, null);
    }

    private void writeJson(OutputStream outputStream, int statusCode, String json, String setCookieHeader) throws IOException {
        String extraHeaders = setCookieHeader == null ? null : "Set-Cookie: " + setCookieHeader + "\r\n";
        writeResponse(outputStream, statusCode, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8), extraHeaders);
    }

    private void writeAttachment(OutputStream outputStream, String contentType, byte[] body, String fileName) throws IOException {
        String extraHeaders = "Content-Disposition: attachment; filename=\"" + fileName + "\"\r\n";
        writeResponse(outputStream, 200, contentType, body, extraHeaders);
    }

    private void writeRedirect(OutputStream outputStream, String location) throws IOException {
        writeRedirect(outputStream, location, null);
    }

    private void writeRedirect(OutputStream outputStream, String location, String setCookie) throws IOException {
        byte[] body = new byte[0];
        String extraHeaders = "Location: " + location + "\r\n";
        if (setCookie != null) {
            extraHeaders += "Set-Cookie: " + setCookie + "\r\n";
        }
        writeResponse(outputStream, 302, "text/plain; charset=utf-8", body, extraHeaders);
    }

    private void writeResponse(OutputStream outputStream, int statusCode, String contentType, byte[] body, String extraHeaders) throws IOException {
        String statusText = switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 302 -> "Found";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            default -> "Internal Server Error";
        };

        String headers = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + (extraHeaders == null ? "" : extraHeaders)
                + "Connection: close\r\n\r\n";

        outputStream.write(headers.getBytes(StandardCharsets.UTF_8));
        outputStream.write(body);
        outputStream.flush();
    }
}
