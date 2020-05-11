@echo off
REM
REM Builds a module specified with the first argument
REM $Id: buildModule.bat,v 1.8 2004/07/20 23:33:38 pimp-rod Exp $
REM

setlocal

if not "%TCOO_HOME%" == "" goto tcooHome
    echo Warning: TCOO_HOME not set! Defaulting to parent directory.
    set TCOO_HOME=..
:tcooHome

set MODULE_NAME=%1
shift

set ARGS=%1
shift
:moreArgs
if "%1" == "" goto endArgs
    set ARGS=%ARGS% %1
    shift
    goto moreArgs
:endArgs

echo Building module: "%MODULE_NAME%" located in: "%TCOO_HOME%\%MODULE_NAME%"

if not "%JAVA_HOME%" == "" goto javaHome
    echo Error: JAVA_HOME is not set! Please set JAVA_HOME.
    goto end
:javaHome

if exist "%JAVA_HOME%\bin\java.exe" goto java
    echo Error: Could not find "%JAVA_HOME%\bin\java.exe".
    echo        Please set JAVA_HOME to installed Java SDK.
    goto end
:java

if exist "%JAVA_HOME%\lib\tools.jar" goto javaTools
    echo Error: Could not find "%JAVA_HOME%\lib\tools.jar".
    echo        Please set JAVA_HOME to installed Java SDK.
    goto end
:javaTools

set JAVA="%JAVA_HOME%\bin\java.exe"

:runAnt
%JAVA% "-Dopenorb.module.name=%MODULE_NAME%" "-Dopenorb.home.path=%TCOO_HOME%" -jar %TCOO_HOME%\tools\build.jar %ARGS%
if not errorlevel 1 goto end
echo Error: Failed to build "%MODULE_NAME%" with arguments {%ARGS%}

:end
endlocal

