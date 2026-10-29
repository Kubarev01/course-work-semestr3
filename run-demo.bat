@echo off
setlocal
cd /d "%~dp0"
if exist "dist\releaseflow.jar" (
  java -jar "dist\releaseflow.jar" --spring.profiles.active=demo %*
) else if exist "target\releaseflow-0.0.1-SNAPSHOT.jar" (
  java -jar "target\releaseflow-0.0.1-SNAPSHOT.jar" --spring.profiles.active=demo %*
) else (
  echo Build first: mvnw.cmd verify
  exit /b 1
)
