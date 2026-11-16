@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
call "%~dp0env.bat"

if not defined JAVA_HOME (
    echo Не найдена Java. Установите JDK 8 или новее.
    pause
    exit /b 1
)
call "%~dp0server_running.bat"
if not errorlevel 1 (
    echo Сервер уже запущен: http://localhost:8080/bank/
    exit /b 0
)
if not exist tomcat\webapps\bank.war (
    call "%~dp0build.bat"
    if errorlevel 1 (
        pause
        exit /b 1
    )
)

echo Запуск сервера...
call "%~dp0tomcat\bin\startup.bat"
echo.
echo Приложение будет доступно через несколько секунд:
echo http://localhost:8080/bank/
endlocal
