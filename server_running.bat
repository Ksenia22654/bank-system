@echo off
rem Код возврата 0, если сервер этого проекта уже запущен, иначе 1. Вызывается после env.bat.
powershell -NoProfile -ExecutionPolicy Bypass -Command "$h = $env:BANK_HOME; $p = Get-WmiObject Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -and $_.CommandLine.Contains('bank.home') -and $_.CommandLine.Contains($h) }; if ($p) { exit 0 } else { exit 1 }"
