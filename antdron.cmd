@echo off
rem Construye y ejecuta el proyecto sin Maven. Requiere un JDK 17 o superior.
rem Uso: antdron.cmd [ejecutar ^| consola ^| probar ^| xp ... ^| compilar ^| limpiar]
setlocal
cd /d "%~dp0"
chcp 65001 >nul
where java >nul 2>nul || (echo ERROR: no se encontro "java". Instale un JDK 17 o superior. & exit /b 2)
java "%~dp0herramientas\Construir.java" %*
exit /b %ERRORLEVEL%
