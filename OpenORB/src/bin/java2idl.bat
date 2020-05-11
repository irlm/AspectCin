@echo off
REM
REM $Id: java2idl.bat,v 1.5 2003/04/29 16:06:02 mrumpf Exp $
REM

rem
rem Set the TCOO_HOME variable
rem
if "%TCOO_HOME%" == "" goto setHome
    if exist "%TCOO_HOME%\OpenORB\bin\java2idl.bat" goto tcooHome
        echo Error: Couldn't set TCOO_HOME!
        goto end
    :setHome
    echo Warning: TCOO_HOME not set! Trying with default... 
    set TCOO_HOME=.
    if exist "%TCOO_HOME%\OpenORB\bin\java2idl.bat" goto tcooHome
        set TCOO_HOME=..
    if exist "%TCOO_HOME%\OpenORB\bin\java2idl.bat" goto tcooHome
        set TCOO_HOME=..\..
    if exist "%TCOO_HOME%\OpenORB\bin\java2idl.bat" goto tcooHome
        set TCOO_HOME=
        echo Error: Couldn't set TCOO_HOME!
        goto end
:tcooHome


rem
rem Set up the Java environment
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

set JAVA=%JAVA_HOME%\bin\java.exe


rem
rem Collect jars for the bootclasspath
rem
set BOOTCLASSPATH=
for %%i in (%TCOO_HOME%\OpenORB\lib\endorsed\*.jar) do call set BOOTCLASSPATH=%%~fi;%%BOOTCLASSPATH%%


rem
rem Launch the application
rem
%JAVA% -Xbootclasspath/p:%BOOTCLASSPATH% -Dopenorb.env.cp=%CLASSPATH% -Dopenorb.home.path=%TCOO_HOME% -jar %TCOO_HOME%\tools\lib\launcher.jar org.openorb.compiler.rmi.JavaToIdl %*

:end
