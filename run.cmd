@echo off
setlocal

if not exist out mkdir out

javac -d out ^
    src/com/arlind/portfolio/App.java ^
    src/com/arlind/portfolio/SmokeTest.java ^
    src/com/arlind/portfolio/http/PortfolioServer.java ^
    src/com/arlind/portfolio/model/Profile.java ^
    src/com/arlind/portfolio/model/Project.java ^
    src/com/arlind/portfolio/model/Message.java ^
    src/com/arlind/portfolio/model/Skill.java ^
    src/com/arlind/portfolio/security/PasswordHasher.java ^
    src/com/arlind/portfolio/repository/PortfolioRepository.java ^
    src/com/arlind/portfolio/repository/InMemoryPortfolioRepository.java ^
    src/com/arlind/portfolio/repository/DatabaseConfig.java ^
    src/com/arlind/portfolio/repository/JdbcPortfolioRepository.java
if errorlevel 1 exit /b 1

xcopy src\static out\static /E /I /Y >nul

set "CLASSPATH_VALUE=out"
for %%F in (lib\mysql-connector-j*.jar) do set "CLASSPATH_VALUE=out;%%~fF"

java -cp "%CLASSPATH_VALUE%" com.arlind.portfolio.App
