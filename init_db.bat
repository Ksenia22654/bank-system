@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
rem Создаёт базу данных из SQL-скрипта средствами СУБД (утилита ij).
rem Приложение делает то же самое при первом запуске, если базы ещё нет.

if exist data\bankdb (
    echo База данных уже существует: data\bankdb
    echo Чтобы создать её заново, остановите сервер ^(stop.bat^) и удалите эту папку.
    exit /b 1
)
if not exist data mkdir data

java -Dderby.ui.codeset=UTF-8 -Dderby.stream.error.file=data\derby.log ^
    "-Dij.connection.bankdb=jdbc:derby:data/bankdb;create=true" ^
    -cp "lib\derby-10.14.2.0.jar;lib\derbytools-10.14.2.0.jar" ^
    org.apache.derby.tools.ij sql\create_db.sql
endlocal
