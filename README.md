# Arlind Hyseni Portfolio CMS

Ky projekt eshte nje `Portfolio CMS` i ndertuar me `Java`, `MySQL`, `HTML`, `CSS` dhe `JavaScript`, pa framework te rende backend-i. Qellimi i tij nuk eshte vetem te shfaqe nje faqe personale, por te funksionoje si nje sistem i plote administrimi ku mund te:

- menaxhosh profilin personal
- ndryshosh password-in e adminit
- shtosh, perditesosh dhe fshish projekte
- lexosh dhe fshish mesazhe nga vizitoret
- ngarkosh foto profili lokale
- shfaqesh publikisht nje faqe responsive dhe me stil modern `dark neon`

Ky `README` eshte shkruar si dokument teknik dhe njekohesisht si material mesimi, qe ti ta kuptosh jo vetem si perdoret projekti, por edhe pse eshte ndertuar keshtu.

## 1. Qellimi i Projektit

Ky aplikacion eshte krijuar si nje portfolio personale profesionale per `Arlind Hyseni`, por me logjike te ngjashme me nje `CMS` te vogel.

Do te thote:

- faqja publike nuk eshte vetem `HTML static`
- te dhenat nuk jane vetem te shkruara ne file
- profili, projektet dhe mesazhet ruhen ne databaze
- admini ka nje panel te vecante per menaxhim

Pra, projekti eshte nje kombinim midis:

- `public website`
- `admin dashboard`
- `custom Java backend`
- `MySQL database`

## 2. Cfare Permban Aplikacioni

Ky aplikacion aktualisht permban:

- Login sekret per admin
- Session me cookie te thjeshte
- CSRF protection me token
- Password hashing ne backend
- Upload lokal te fotos se profilit
- Faqe publike dinamike nga databaza
- Panel admin me kontroll te plote
- CRUD per projektet
- Delete per mesazhet
- Profile settings dinamike
- Skills dinamike nga databaza
- Responsive design per desktop dhe mobile
- Akses nga pajisje te tjera ne rrjet lokal me `0.0.0.0`

## 3. Hierarkia e Projektit

Struktura e projektit eshte:

```text
arlindhyseni/
|-- lib/
|-- out/
|-- src/
|   |-- com/
|   |   `-- arlind/
|   |       `-- portfolio/
|   |           |-- App.java
|   |           |-- http/
|   |           |   `-- PortfolioServer.java
|   |           |-- model/
|   |           |   |-- Profile.java
|   |           |   |-- Project.java
|   |           |   |-- Message.java
|   |           |   `-- Skill.java
|   |           |-- repository/
|   |           |   |-- PortfolioRepository.java
|   |           |   |-- InMemoryPortfolioRepository.java
|   |           |   |-- JdbcPortfolioRepository.java
|   |           |   `-- DatabaseConfig.java
|   |           `-- security/
|   |               `-- PasswordHasher.java
|   `-- static/
|       |-- index.html
|       |-- login.html
|       `-- admin.html
|-- uploads/
|-- schema.sql
|-- run.cmd
`-- README.md
```

## 4. Shpjegimi i Hierarkise

### `src/com/arlind/portfolio/App.java`

Ky eshte entry point i aplikacionit.

Detyrat e tij:

- lexon porten nga `PORTFOLIO_PORT`
- zgjedh nese do perdore `in-memory` ose `MySQL`
- ndez serverin

Pra kjo klase eshte pika ku aplikacioni “fillon frymemarrjen”.

### `src/com/arlind/portfolio/http/PortfolioServer.java`

Kjo eshte zemra e aplikacionit.

Ajo ben:

- hap socket server ne porten e caktuar
- pranon kerkesat HTTP
- ndan kerkesat sipas `GET`, `POST`, `PUT`, `DELETE`
- kontrollon autentikimin
- kontrollon `CSRF token`
- kthen HTML ose JSON
- sherben edhe fotot nga folderi `uploads`

Me fjale te thjeshta: kjo klase sillet si nje “mini web framework” i shkruar me dore.

### `src/com/arlind/portfolio/model/`

Ketu jane objektet qe perfaqesojne te dhenat.

#### `Profile.java`

Mban:

- full name
- title
- bio
- philosophy
- image url
- age
- location
- email
- password

Ky model eshte ai qe perfaqeson rreshtin e vetem ne tabelen `profile_settings`.

#### `Project.java`

Mban:

- id
- title
- description
- stack

Perfaqeson nje projekt ne portfolio.

#### `Message.java`

Mban:

- id
- sender name
- email
- body
- created at

Perfaqeson nje mesazh te ardhur nga forma e kontaktit.

#### `Skill.java`

Mban:

- id
- emrin e skill-it
- kategorine

Perfaqeson nje teknologji ose aftesi qe shfaqet ne faqen publike.

### `src/com/arlind/portfolio/repository/`

Kjo shtrese merret me komunikimin me te dhenat.

#### `PortfolioRepository.java`

Eshte kontrata.

Ai percakton:

- cfare metodash duhet te kete repository
- si merren dhe ruhen te dhenat

Kjo e ben kodin me te paster sepse serveri nuk varet nga implementimi konkret.

#### `InMemoryPortfolioRepository.java`

Ky eshte implementim per testim lokal pa databaze.

Pse eshte i dobishem:

- kur databaza s’eshte gati
- kur do demo te shpejte
- kur do kuptosh logjiken pa u marre me MySQL

#### `JdbcPortfolioRepository.java`

Ky eshte implementimi real me `JDBC`.

Ai:

- lidhet me MySQL
- lexon `profile_settings`
- lexon `skills`
- lexon `projects`
- ruan `messages`
- ndryshon password-in
- ben CRUD per projektet

Ky eshte vendi ku backend-i flet me databazen.

#### `DatabaseConfig.java`

Mban:

- URL e databazes
- username
- password

Pra ndan konfigurimin nga logjika.

### `src/com/arlind/portfolio/security/PasswordHasher.java`

Kjo klase merret me:

- kthimin e password-it ne hash
- krahasimin e password-it plain me hash-in e ruajtur

Aktualisht perdor `SHA-256`.

Kjo eshte me mire se ruajtja plain text, por ne nje sistem real hapi tjeter do ishte:

- `PBKDF2`
- ose `bcrypt`
- ose `Argon2`

### `src/static/`

Kjo pjese permban faqet frontend.

#### `index.html`

Faqja publike.

Lexon nga API:

- profilin
- skills
- projektet

Shfaq:

- hero section
- about
- skills
- projects
- contact form

#### `login.html`

Faqja sekrete e hyrjes ne dashboard.

#### `admin.html`

Paneli i administrimit.

Ka:

- profile settings
- password change
- create project
- edit/delete project
- read/delete messages
- logout
- image upload

## 5. Si Funksionon Rrjedha e Kerkeses

Kur nje vizitor hap faqen:

1. browser kerkon `GET /`
2. serveri kthen `index.html`
3. `index.html` ben `fetch` te:
   - `/api/profile`
   - `/api/skills`
   - `/api/projects`
4. serveri i merr nga databaza
5. faqja mbushet me te dhena reale

Kur admini hyn:

1. hap `/login`
2. serveri kthen `login.html`
3. forma ben `POST /login`
4. serveri verifikon username dhe password
5. krijohet cookie e sesionit
6. admini ridrejtohet te `/admin`

Kur admini ndryshon profilin:

1. admin panel ben `POST /api/settings`
2. serveri verifikon sesionin dhe CSRF
3. repository ben `UPDATE profile_settings`
4. faqja publike ne refresh lexon te dhenat e reja

## 6. Struktura e Databazes

Ky projekt perdor tabelat:

- `profile_settings`
- `skills`
- `projects`
- `messages`

### `profile_settings`

Mban vetem nje rresht.

Fushat:

- `id`
- `full_name`
- `title`
- `bio`
- `philosophy`
- `image_url`
- `age`
- `location`
- `email`
- `password`

### `skills`

Mban listen e aftesive teknike.

Fushat:

- `id`
- `skill_name`
- `category`

### `projects`

Mban projektet e portfolio-s.

Fushat:

- `id`
- `title`
- `description`
- `stack`

### `messages`

Mban mesazhet nga vizitoret.

Fushat:

- `id`
- `sender_name`
- `email`
- `body`
- `created_at`

## 7. SQL i Plotë i Projektit

Per kete projekt perdor [schema.sql](C:\Users\Arlind Hyseni\OneDrive - Kosovo Research and Education Network (KREN)\Desktop\arlindhyseni\schema.sql).

Nese do ta ekzekutosh direkt ne `phpMyAdmin`, kjo eshte permbajtja:

```sql
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
```

## 8. Si ta Nisesh Lokalisht

### Kerkesat

Duhet te kesh:

- `Java 17`
- `XAMPP` ose nje `MySQL server`
- `mysql-connector-j.jar` te folderi `lib`

### Komandat e Nisjes

Ne PowerShell:

```powershell
$env:PORTFOLIO_PORT="9191"
$env:PORTFOLIO_DATA_MODE="mysql"
$env:PORTFOLIO_DB_URL="jdbc:mysql://localhost:3306/portfolio_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:PORTFOLIO_DB_USER="root"
$env:PORTFOLIO_DB_PASSWORD=""
cmd /c run.cmd
```

### Faqet kryesore

- publikja: `http://localhost:9191`
- login: `http://localhost:9191/login`
- admin: `http://localhost:9191/admin`

### Login fillestar

- username: `admin`
- password: `admin123`

## 9. Si ta Hapesh nga Telefoni

Ky projekt tani ben bind ne `0.0.0.0`, qe do te thote se serveri nuk eshte i dukshëm vetem nga kompjuteri yt, por edhe nga pajisje te tjera ne rrjet lokal.

Hapat:

1. Nise aplikacionin.
2. Hape nje terminal tjeter.
3. Shkruaj:

```powershell
ipconfig
```

4. Gjej `IPv4 Address`, p.sh. `192.168.1.15`
5. Sigurohu qe telefoni dhe kompjuteri jane ne te njejtin Wi‑Fi
6. Nga telefoni hape:

```text
http://192.168.1.15:9191
```

Nese s’hapet:

- kontrollo firewall-in
- sigurohu qe serveri po punon
- sigurohu qe IP-ja eshte e sakte

## 10. Siguria e Aplikacionit

Ky projekt ka disa masa sigurie baze:

### Login i ndare

`/admin` nuk eshte me ne navbar publik.

### Session Cookie

Pas login-it ruhet nje cookie sesioni qe perdoret per admin access.

### CSRF Protection

Format perdorin:

- cookie `csrf_token`
- hidden input `csrf_token`

Backend verifikon qe:

- tokeni ne cookie
- tokeni ne kerkese

jane te njejte.

### Password Hashing

Password-i i ri nuk ruhet me plain text, por me:

- `sha256$...`

Kjo eshte me mire se plain text, por ende jo niveli maksimal i sigurise.

### Upload i kufizuar

Fotoja ruhet lokalisht ne `uploads/`.

Emri i file-it pastrohet nga karaktere te rrezikshme, por hapi tjeter profesional do te ishte:

- kontrolli i madhesise se file-it
- kontrolli i tipit real MIME
- storage me i sigurt

## 11. Si Funksionon Upload i Fotos

Kur admini zgjedh nje foto:

1. `admin.html` lexon file-in me `FileReader`
2. e kthen ne `base64`
3. dergon `POST /api/upload-image`
4. serveri e ruan ne folderin `uploads`
5. serveri kthen rrugen `/uploads/...`
6. forma e profilit ruan kete URL ne `profile_settings.image_url`
7. faqja publike e lexon dhe e shfaq

## 12. API Endpoint-et Kryesore

### Publike

- `GET /`
- `GET /api/profile`
- `GET /api/skills`
- `GET /api/projects`
- `POST /api/contact`

### Admin

- `GET /login`
- `POST /login`
- `GET /admin`
- `POST /logout`
- `GET /api/messages`
- `POST /api/projects`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`
- `DELETE /api/messages/{id}`
- `POST /api/settings`
- `POST /api/change-password`
- `POST /api/upload-image`

## 13. Pergatitja per Deploy Online

Ky projekt eshte pergatitur qe te mund te kaloje ne nje server real.

Per deploy profesional, dy rruget me te arsyeshme jane:

- `Docker`
- `VPS / cloud VM`

Per kete arsye po shtojme me poshte nje menyre te qarte deploy.

## 14. Deploy me Docker

Ky projekt nuk perdor Maven ose Gradle, prandaj Dockerfile duhet te kompilojë direkt me `javac`.

Skedari [Dockerfile](C:\Users\Arlind Hyseni\OneDrive - Kosovo Research and Education Network (KREN)\Desktop\arlindhyseni\Dockerfile) perdoret per te ndertuar nje imazh te gatshem per server.

### Build

```powershell
docker build -t arlind-portfolio-cms .
```

### Run

```powershell
docker run -p 9191:9191 ^
  -e PORTFOLIO_PORT=9191 ^
  -e PORTFOLIO_DATA_MODE=mysql ^
  -e PORTFOLIO_DB_URL="jdbc:mysql://host.docker.internal:3306/portfolio_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" ^
  -e PORTFOLIO_DB_USER=root ^
  -e PORTFOLIO_DB_PASSWORD= ^
  arlind-portfolio-cms
```

Nese databaza eshte ne nje server tjeter, nderro `PORTFOLIO_DB_URL`.

## 15. Deploy ne VPS ose Cloud

Per nje deploy real online, struktura e rekomanduar eshte:

- nje server Linux me Java 17
- nje MySQL server i ndare ose managed
- `nginx` si reverse proxy
- nje domain

Rrjedha profesionale:

1. kopjo projektin ne server
2. kopjo `mysql-connector-j.jar` ne `lib`
3. ekzekuto `schema.sql` ne MySQL
4. kompilo me `run.cmd` ose me nje variant `run.sh`
5. vendos nje service qe serveri te ndizet automatikisht
6. vendos `nginx` per portin `80/443`
7. shto SSL me `Let's Encrypt`

## 16. Skedaret Shtese per Deploy

Ky projekt duhet te kete:

- `Dockerfile`
- `.dockerignore`
- `.gitignore`

Qe:

- te mos ngarkohen file-at e panevojshem
- te mos futet `out/` ne repo
- te mos futen log-et e perkohshme

## 17. Kufizimet Aktualisht

Ky projekt eshte funksional, por disa gjera mund te permiresohen:

- password hashing me `PBKDF2`, `bcrypt` ose `Argon2`
- upload image validation me te forte
- logout me session store me te paster
- audit log per admin actions
- status `read/unread` per mesazhet
- menaxhim i `skills` nga admin paneli
- pagination per projekte dhe mesazhe

## 18. Cfare Ke Mesuar nga Ky Projekt

Nga ky projekt mund te kuptosh ne praktike:

- si ndertohet nje server HTTP me Java pa framework
- si organizohen modelet
- cfare eshte nje repository pattern
- si perdoret JDBC
- si lidhet frontend-i me backend-in
- si funksionon autentikimi bazik
- si funksionon CSRF protection
- si ruhen te dhenat ne MySQL
- si behet upload file
- si behet responsive UI
- si pergatitet nje aplikacion per deploy

Pra ky projekt nuk eshte vetem “nje portfolio”, por nje laborator i vogel ku ke prekur:

- web development
- backend architecture
- databaza
- security
- UI/UX
- deploy mindset

## 19. Hapat e Arsyeshem Pasues

Nese do ta cosh edhe me lart, une do te rekomandoja kete rend:

1. `PBKDF2` ose `bcrypt` per password
2. status `read/unread` per messages
3. CRUD edhe per `skills`
4. `run.sh` per Linux deploy
5. `Dockerfile` dhe `nginx` konfigurim real
6. deploy online me domain

## 20. Permbledhje

Ky aplikacion eshte tashme nje `Portfolio CMS` real me:

- public site dinamik
- admin dashboard
- databaze reale
- login
- CRUD
- settings
- image upload
- responsive design
- network access
- bazat e sigurise

Pra, jo vetem qe e ke ndertuar nje website personal, por ke ndertuar nje sistem qe te meson menyren si mendohet nje produkt software nga fillimi deri te struktura gati per publikim.
