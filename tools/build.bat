@echo off
REM
REM $Id: build.bat,v 1.5 2003/03/14 15:29:09 mrumpf Exp $
REM

setlocal

SET MODULE_NAME=tools

if not "%TCOO_HOME%" == "" goto tcooHome
    echo Warning: TCOO_HOME not set! Defaulting to parent directory. 
    set TCOO_HOME=..
:tcooHome

if exist "%TCOO_HOME%\tools\buildModule.bat" goto doBuild
    echo Error: Could not find "%TCOO_HOME%\tools\buildModule.bat". 
    echo        The tools source distribution is not located in correct directory.
    goto end

:doBuild
call %TCOO_HOME%\tools\buildModule.bat %MODULE_NAME% %*

:end
endlocal
