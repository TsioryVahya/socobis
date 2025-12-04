@echo off
echo ========================================
echo    SOCOBIS - Demarrage WildFly
echo ========================================
echo.

REM Configuration WildFly
set WILDFLY_HOME=G:\ZAVATRA_INSTALENA\wildfly-10.0.0.Final\wildfly-10.0.0.Final
set WILDFLY_BIN=%WILDFLY_HOME%\bin
set WILDFLY_STANDALONE=%WILDFLY_BIN%\standalone.bat

echo Configuration:
echo - WildFly Home: %WILDFLY_HOME%
echo - Script de demarrage: %WILDFLY_STANDALONE%
echo.

REM Verification de l'installation WildFly
echo [1/3] Verification de WildFly...
if not exist "%WILDFLY_HOME%" (
    echo ERREUR: WildFly non trouve dans %WILDFLY_HOME%
    echo Veuillez verifier le chemin d'installation
    pause
    exit /b 1
)

if not exist "%WILDFLY_STANDALONE%" (
    echo ERREUR: Script standalone.bat non trouve dans %WILDFLY_BIN%
    pause
    exit /b 1
)
echo WildFly trouve

REM Verification si WildFly est deja en cours
echo [2/3] Verification si WildFly est deja en cours...
netstat -an | find "8080" | find "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo ATTENTION: Un service ecoute deja sur le port 8080
    echo WildFly est peut-etre deja en cours d'execution
    echo.
    set /p continue="Continuer quand meme ? (o/N): "
    if /i not "%continue%"=="o" (
        echo Demarrage annule
        pause
        exit /b 1
    )
)

REM Configuration des variables d'environnement Java
echo [3/3] Configuration de l'environnement...
if not defined JAVA_HOME (
    echo ATTENTION: JAVA_HOME n'est pas defini
    echo Tentative de detection automatique...
    for /f "tokens=2*" %%i in ('reg query "HKLM\SOFTWARE\JavaSoft\Java Development Kit" /s /v JavaHome 2^>nul ^| find "JavaHome"') do set JAVA_HOME=%%j
    if defined JAVA_HOME (
        echo JAVA_HOME detecte: %JAVA_HOME%
    ) else (
        echo ERREUR: Impossible de detecter JAVA_HOME
        echo Veuillez installer JDK 8 et configurer JAVA_HOME
        pause
        exit /b 1
    )
)

echo JAVA_HOME: %JAVA_HOME%
echo.

REM Options JVM pour WildFly
set JAVA_OPTS=-Xms512m -Xmx1024m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m
echo Options JVM: %JAVA_OPTS%
echo.

echo ========================================
echo      DEMARRAGE DE WILDFLY
echo ========================================
echo.
echo WildFly va demarrer avec les parametres suivants:
echo - Port HTTP: 8080
echo - Port HTTPS: 8443  
echo - Port Management: 9990
echo - Interface: toutes (0.0.0.0)
echo.
echo URLs d'acces:
echo - Application SOCOBIS: http://localhost:8080/socobis
echo - Console d'administration: http://localhost:9990
echo.
echo ATTENTION: Cette fenetre doit rester ouverte
echo Pour arreter WildFly, utilisez Ctrl+C dans cette fenetre
echo.
echo Demarrage en cours...
echo ========================================
echo.

REM Changement vers le repertoire WildFly
cd /d "%WILDFLY_HOME%"

REM Demarrage de WildFly avec binding sur toutes les interfaces
"%WILDFLY_STANDALONE%" -b 0.0.0.0 -bmanagement 0.0.0.0

echo.
echo ========================================
echo      WILDFLY ARRETE
echo ========================================
echo.
pause
