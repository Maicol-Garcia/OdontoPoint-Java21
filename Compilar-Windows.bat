@echo off
cd /d "%~dp0"
call mvn clean package
if errorlevel 1 exit /b 1
if not exist bin mkdir bin
copy /Y target\odontopoint-web-2.0.0.jar bin\odontopoint-web.jar
pause
