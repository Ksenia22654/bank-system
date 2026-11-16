@echo off
rem Общие настройки окружения для запуска и остановки Tomcat.
set "BANK_HOME=%~dp0"
if "%BANK_HOME:~-1%"=="\" set "BANK_HOME=%BANK_HOME:~0,-1%"
set "CATALINA_HOME=%BANK_HOME%\tomcat"
set "CATALINA_BASE=%BANK_HOME%\tomcat"
set "CATALINA_OPTS=-Dbank.home="%BANK_HOME%" -Dfile.encoding=UTF-8"

if defined JAVA_HOME goto :eof
for /f "tokens=2 delims==" %%i in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home"') do set "JAVA_HOME=%%i"
if defined JAVA_HOME set "JAVA_HOME=%JAVA_HOME:~1%"
