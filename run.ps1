$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Force out | Out-Null

javac -d out `
    src/com/arlind/portfolio/App.java `
    src/com/arlind/portfolio/SmokeTest.java `
    src/com/arlind/portfolio/http/PortfolioServer.java `
    src/com/arlind/portfolio/model/Profile.java `
    src/com/arlind/portfolio/model/Project.java `
    src/com/arlind/portfolio/repository/PortfolioRepository.java `
    src/com/arlind/portfolio/repository/InMemoryPortfolioRepository.java `
    src/com/arlind/portfolio/repository/DatabaseConfig.java `
    src/com/arlind/portfolio/repository/JdbcPortfolioRepository.java

Copy-Item -Recurse -Force src/static out/static

$mysqlJar = Get-ChildItem -Path "lib" -Filter "mysql-connector-j*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
$classpath = "out"

if ($mysqlJar) {
    $classpath = "out;$($mysqlJar.FullName)"
}

java -cp $classpath com.arlind.portfolio.App
