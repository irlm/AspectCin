@echo off
REM
REM $Id: build.bat,v 1.9 2003/03/13 16:59:16 mrumpf Exp $
REM

setlocal

SET MODULE_NAME=OpenORB

if not "%TCOO_HOME%" == "" goto tcooHome
    echo Warning: TCOO_HOME not set! Defaulting to parent directory. 
    set TCOO_HOME=..
:tcooHome

if exist "%TCOO_HOME%\tools\buildModule.bat" goto doBuild
    set BUILD_ERROR=TRUE
    echo Error: Could not find "%TCOO_HOME%\tools\buildModule.bat". 
    echo        The tools source distribution is not located in correct directory.
    goto end

:doBuild
call %TCOO_HOME%\tools\buildModule.bat %MODULE_NAME% %*

:end
endlocal
