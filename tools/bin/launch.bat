@echo off
REM
REM Runs an OpenORB application.
REM $Id: launch.bat,v 1.1 2003/04/12 15:13:12 mrumpf Exp $
REM

setlocal

rem
rem Get the TCOO_HOME
rem

if "%TCOO_HOME%" == "" goto setHome
    if exist "%TCOO_HOME%\tools\bin\updateConfig.bat" goto tcooHome
        echo Error: Couldn't set TCOO_HOME!
        goto end
    :setHome
    echo Warning: TCOO_HOME not set! Trying with default...
    set TCOO_HOME=.
    if exist "%TCOO_HOME%\tools\bin\updateConfig.bat" goto tcooHome
        set TCOO_HOME=..
    if exist "%TCOO_HOME%\tools\bin\updateConfig.bat" goto tcooHome
        set TCOO_HOME=..\..
    if exist "%TCOO_HOME%\tools\bin\updateConfig.bat" goto tcooHome
        set TCOO_HOME=
        echo Error: Couldn't set TCOO_HOME!
        goto end
:tcooHome


rem
rem Collect the arguments
rem

set ARGS=%1
shift
:moreArgs
if "%1" == "" goto endArgs
    set ARGS=%ARGS% %1
    shift
    goto moreArgs
:endArgs


rem
rem Get the JAVA_HOME and the java.exe
rem

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

set JAVA=%JAVA_HOME%\bin\java.exe


rem
rem Collect jars for the bootclasspath
rem

set BOOTCLASSPATH=
for %%i in (%TCOO_HOME%\OpenORB\lib\endorsed\*.jar) do call set BOOTCLASSPATH=%%~fi;%%BOOTCLASSPATH%%


rem
rem Launch the application
rem

%JAVA% -Xbootclasspath/p:%BOOTCLASSPATH% "-Dopenorb.home.path=%TCOO_HOME%" -jar %TCOO_HOME%\tools\lib\launcher.jar %ARGS%

:end
endlocal

