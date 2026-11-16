@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
call "%~dp0env.bat"
call "%~dp0tomcat\bin\shutdown.bat"
endlocal
