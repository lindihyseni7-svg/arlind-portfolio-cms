# Deploy Guide

Ky dokument eshte guida praktike per ta kaluar projektin nga kompjuteri yt ne `GitHub` dhe pastaj ne nje server real online.

## 1. Para se te fillosh

Sigurohu qe ke:

- llogari `GitHub`
- `git` te instaluar
- projektin funksional lokalisht
- `mysql-connector-j.jar` te folderi `lib`

## 2. Si ta dergosh ne GitHub

Nga folderi i projektit:

```powershell
git add .
git status
git commit -m "Initial portfolio CMS"
```

Pastaj krijo nje repo te re bosh ne GitHub, p.sh.:

- emri: `arlind-portfolio-cms`

Pas krijimit te repo-s, GitHub do te te jape nje URL si:

```text
https://github.com/USERNAME/arlind-portfolio-cms.git
```

Shto remote:

```powershell
git remote add origin https://github.com/USERNAME/arlind-portfolio-cms.git
```

Nese branch-i lokal del `master`, mund ta kalosh ne `main`:

```powershell
git branch -M main
```

Pastaj bej push:

```powershell
git push -u origin main
```

## 3. Si ta ndertosh me Docker

Build:

```powershell
docker build -t arlind-portfolio-cms .
```

Run:

```powershell
docker run -p 9191:9191 ^
  -e PORTFOLIO_PORT=9191 ^
  -e PORTFOLIO_DATA_MODE=mysql ^
  -e PORTFOLIO_DB_URL="jdbc:mysql://host.docker.internal:3306/portfolio_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" ^
  -e PORTFOLIO_DB_USER=root ^
  -e PORTFOLIO_DB_PASSWORD= ^
  arlind-portfolio-cms
```

## 4. Deploy ne VPS

Ne nje server Linux:

1. Instalo `Docker`
2. Klono repo-n:

```bash
git clone https://github.com/USERNAME/arlind-portfolio-cms.git
cd arlind-portfolio-cms
```

3. Vendos `mysql-connector-j.jar` te `lib/`
4. Build image:

```bash
docker build -t arlind-portfolio-cms .
```

5. Nise container:

```bash
docker run -d --name arlind-portfolio-cms \
  -p 9191:9191 \
  -e PORTFOLIO_PORT=9191 \
  -e PORTFOLIO_DATA_MODE=mysql \
  -e PORTFOLIO_DB_URL="jdbc:mysql://YOUR_DB_HOST:3306/portfolio_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e PORTFOLIO_DB_USER=YOUR_DB_USER \
  -e PORTFOLIO_DB_PASSWORD=YOUR_DB_PASSWORD \
  arlind-portfolio-cms
```

## 5. Rruga me profesionale me tej

Pasi ta vendosesh online, hapat e ardhshem jane:

- vendos `nginx` si reverse proxy
- aktivizo `HTTPS`
- bli ose lidhe nje domain
- ruaj secrets si env vars ne server
- largo password-et demo

## 6. Rekomandim praktik

Rruga me e mire per ty tani eshte:

1. GitHub
2. commit i pare
3. push online
4. deploy ne VPS ose Docker host

Kjo te jep edhe projekt real per portfolio, edhe eksperience reale ne workflow-in e zhvillimit.
