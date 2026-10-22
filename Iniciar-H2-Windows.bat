@echo off
cd /d "%~dp0"
set "APP_JAR=bin\odontopoint-web.jar"
if not exist "%APP_JAR%" set "APP_JAR=target\odontopoint-web-2.0.0.jar"
if not exist "%APP_JAR%" (
 echo Compile primero con Compilar-Windows.bat. Requiere JDK 21.
 pause
 exit /b 1
)
java -jar "%APP_JAR%" --spring.profiles.active=h2
pause
