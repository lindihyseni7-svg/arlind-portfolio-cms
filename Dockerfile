FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src ./src
COPY lib ./lib
COPY run.cmd ./run.cmd

RUN mkdir -p out && \
    javac -d out \
      src/com/arlind/portfolio/App.java \
      src/com/arlind/portfolio/SmokeTest.java \
      src/com/arlind/portfolio/http/PortfolioServer.java \
      src/com/arlind/portfolio/model/Profile.java \
      src/com/arlind/portfolio/model/Project.java \
      src/com/arlind/portfolio/model/Message.java \
      src/com/arlind/portfolio/model/Skill.java \
      src/com/arlind/portfolio/security/PasswordHasher.java \
      src/com/arlind/portfolio/repository/PortfolioRepository.java \
      src/com/arlind/portfolio/repository/InMemoryPortfolioRepository.java \
      src/com/arlind/portfolio/repository/DatabaseConfig.java \
      src/com/arlind/portfolio/repository/JdbcPortfolioRepository.java && \
    cp -r src/static out/static && \
    mkdir -p uploads

EXPOSE 9191

ENV PORTFOLIO_PORT=9191

CMD ["sh", "-lc", "CP=out; JAR=$(ls lib/mysql-connector-j*.jar 2>/dev/null | head -n 1); if [ -n \"$JAR\" ]; then CP=\"out:$JAR\"; fi; java -cp \"$CP\" com.arlind.portfolio.App"]
