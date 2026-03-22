CREATE DATABASE IF NOT EXISTS portfolio_db;
USE portfolio_db;

DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS profile_settings;

CREATE TABLE profile_settings (
    id INT NOT NULL PRIMARY KEY,
    full_name VARCHAR(140) NOT NULL,
    title VARCHAR(180) NOT NULL,
    bio TEXT NOT NULL,
    philosophy TEXT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    age INT NOT NULL,
    location VARCHAR(140) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password VARCHAR(180) NOT NULL
);

CREATE TABLE skills (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(120) NOT NULL,
    category VARCHAR(120) NOT NULL
);

CREATE TABLE projects (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    stack VARCHAR(180) NOT NULL
);

CREATE TABLE messages (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sender_name VARCHAR(140) NOT NULL,
    email VARCHAR(180) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO profile_settings (
    id, full_name, title, bio, philosophy, image_url, age, location, email, password
) VALUES (
    1,
    'Arlind Hyseni',
    'Full-Stack Software Engineer',
    'Ndertoj produkte moderne me Java, API te pastra dhe databaza reale MySQL. Qellimi im eshte te kombinoj arkitekture te qarte, nderfaqe profesionale dhe eksperience reale ne software engineering.',
    'Programimi nuk është thjesht kodim; është arkitektura e së ardhmes. Si student 19-vjeçar në FSHMN dhe zhvillues i apasionuar, unë besoj se inovacioni lind aty ku matematika takon imagjinatën. Çdo projekt që ndërtoj është një përpjekje për të thjeshtuar kompleksitetin.',
    'https://ui-avatars.com/api/?name=Arlind+Hyseni&background=0f172a&color=06b6d4&size=280',
    19,
    'Kosove',
    'arlind@example.com',
    'admin123'
);

INSERT INTO skills (skill_name, category) VALUES
('HTML', 'Frontend'),
('CSS', 'Frontend'),
('JavaScript', 'Frontend'),
('Bootstrap', 'Frontend'),
('GitHub', 'Tools'),
('Java', 'Backend'),
('PHP', 'Backend'),
('MySQL (Advanced)', 'Database');

INSERT INTO projects (title, description, stack) VALUES
('Portfolio Personal', 'CMS personal me Java, admin dashboard, login, settings dinamike dhe public site me dark neon design.', 'Java, MySQL, HTML, CSS, JavaScript'),
('Menaxhim Studentor', 'Sistem i konceptuar per ruajtjen e studenteve, lendeve dhe notave me backend te organizuar.', 'Java, SQL'),
('Kontakt Form API', 'Formular kontakti qe ruan mesazhet ne databaze dhe i shfaq ne panelin admin.', 'Java, REST, MySQL');
