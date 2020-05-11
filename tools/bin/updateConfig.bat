rem @echo off
REM
REM $Id: updateConfig.bat,v 1.2 2004/03/22 09:26:03 lkuehne Exp $
REM

setlocal


rem
rem Get the JAVA_HOME and the jar.exe
rem

if not "%JAVA_HOME%" == "" goto javaHome
    echo Error: JAVA_HOME is not set! Please set JAVA_HOME.
    goto end
:javaHome

if exist "%JAVA_HOME%\bin\jar.exe" goto jar
    echo Error: Could not find "%JAVA_HOME%\bin\jar.exe".
    echo        Please set JAVA_HOME to installed Java SDK.
    goto end
:jar

set JAR=%JAVA_HOME%\bin\jar.exe


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
rem Get the module name
rem
set MODULE=%1
if "%MODULE%" == "" goto showUsage
    if not exist "%TCOO_HOME%\%MODULE%" goto showUsage


rem
rem Get the config file name
rem

set CONFIG_FILE=%2
if "%CONFIG_FILE%" == "" goto showUsage
set CONFIG_FILE_NAME=%~n2%~x2


set CONFIG_FOLDER=org\openorb\config

mkdir %TCOO_HOME%\tmp\%CONFIG_FOLDER%

copy %CONFIG_FILE% %TCOO_HOME%\tmp\%CONFIG_FOLDER%\%CONFIG_FILE_NAME%
if not "%errorlevel%" == "0" goto end

for %%i in (%TCOO_HOME%\%MODULE%\lib\openorb*.jar) do call %JAR% -uf %%i -C %TCOO_HOME%\tmp %CONFIG_FOLDER%\%CONFIG_FILE_NAME%

rmdir /S /Q %TCOO_HOME%\tmp

goto end

:showUsage
echo Usage: updateConfig (module-name) (xml-file)
echo     (module-name)  The name of a module: e.g. InterfaceRepository.
echo                    This name is used to find the following path:
echo                    %TCOO_HOME%\InterfaceRepository\lib\openorb*.jar.
echo     (xml-file)     The name of the config file: e.g. pss.xml.
goto end

:end
endlocal

