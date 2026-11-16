@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

call "%~dp0env.bat"
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo Не найден JDK. Установите JDK 8 или новее и укажите его в JAVA_HOME.
    exit /b 1
)

call "%~dp0server_running.bat"
if not errorlevel 1 (
    echo Сервер запущен. Сначала остановите его: stop.bat
    exit /b 1
)

echo Сборка приложения...
if exist build rmdir /s /q build
mkdir build\war\WEB-INF\classes
mkdir build\war\WEB-INF\lib
mkdir build\war\WEB-INF\sql

"%JAVA_HOME%\bin\javac" -encoding UTF-8 -source 8 -target 8 -Xlint:-options ^
    -cp "tomcat\lib\servlet-api.jar;tomcat\lib\jsp-api.jar;tomcat\lib\el-api.jar" ^
    -d build\war\WEB-INF\classes ^
    src\com\bank\exception\*.java src\com\bank\model\*.java src\com\bank\util\*.java src\com\bank\dao\*.java ^
    src\com\bank\service\*.java src\com\bank\report\*.java src\com\bank\controller\*.java src\com\bank\web\*.java src\com\bank\tag\*.java
if errorlevel 1 (
    echo Ошибка компиляции.
    exit /b 1
)

xcopy /e /i /y /q web build\war >nul
copy /y sql\create_db.sql build\war\WEB-INF\sql\ >nul
copy /y lib\derby-*.jar build\war\WEB-INF\lib\ >nul
copy /y lib\jstl-*.jar build\war\WEB-INF\lib\ >nul

if not exist dist mkdir dist
"%JAVA_HOME%\bin\jar" cf dist\bank.war -C build\war .
if errorlevel 1 (
    echo Не удалось упаковать приложение.
    exit /b 1
)

if exist tomcat\webapps\bank rmdir /s /q tomcat\webapps\bank
if exist tomcat\work\Catalina\localhost\bank rmdir /s /q tomcat\work\Catalina\localhost\bank
copy /y dist\bank.war tomcat\webapps\bank.war >nul

echo Готово: dist\bank.war установлен в Tomcat.
endlocal
