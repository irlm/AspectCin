@echo off
REM
REM Set the runtime environment
REM $Id: setEnv.bat,v 1.3 2003/02/17 02:18:54 pimp-rod Exp $
REM

if not "%TCOO_ENV_SET%" == "TRUE" goto setEnv
echo Restoring old values
set TCOO_HOME=%TCOO_ENV_OLD_TCOO_HOME%
set PATH=%TCOO_ENV_OLD_PATH%
set _JAVA_OPTIONS=%TCOO_ENV_OLD_JAVA_OPTIONS%

set TCOO_ENV_SET=
goto end
:setEnv

echo Setting environment run again to restore old values
set TCOO_ENV_SET=TRUE
set TCOO_ENV_OLD_TCOO_HOME=%TCOO_HOME%
set TCOO_ENV_OLD_PATH=%PATH%
set TCOO_ENV_OLD_JAVA_OPTIONS=%_JAVA_OPTIONS%

if "%TCOO_HOME%" == "" goto setHome
    if exist "%TCOO_HOME%\tools\bin\setEnv.bat" goto tcooHome
        echo Error: Couldn't set TCOO_HOME!
        goto end
    :setHome
    echo Warning: TCOO_HOME not set! Trying with default... 
    set TCOO_HOME=.
    if exist "%TCOO_HOME%\tools\bin\setEnv.bat" goto tcooHome
        set TCOO_HOME=..
    if exist "%TCOO_HOME%\tools\bin\setEnv.bat" goto tcooHome
        set TCOO_HOME=..\..
    if exist "%TCOO_HOME%\tools\bin\setEnv.bat" goto tcooHome
        set TCOO_HOME=
        echo Error: Couldn't set TCOO_HOME!
        goto end
:tcooHome

REM expand TCOO_HOME
for /D %%i in (%TCOO_HOME%) do call set TCOO_HOME=%%~fi

set TCOO_PATH=
for /D %%i in (%TCOO_HOME%\*) do call set TCOO_PATH=%%~fi\bin;%%TCOO_PATH%%

set PATH=%TCOO_PATH%%PATH%

set _JAVA_OPTIONS=-Dorg.omg.CORBA.ORBClass=org.openorb.orb.core.ORB 
set _JAVA_OPTIONS=%_JAVA_OPTIONS% -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.orb.core.ORBSingleton 
set _JAVA_OPTIONS=%_JAVA_OPTIONS% -Djava.class.path=%TCOO_HOME%\tools\lib\launcher.jar
set _JAVA_OPTIONS=%_JAVA_OPTIONS% -Dopenorb.home.path=%TCOO_HOME%
set _JAVA_OPTIONS=%_JAVA_OPTIONS% -Djava.system.class.loader=org.openorb.util.launcher.ProjectClassLoader

for /D %%i in (%TCOO_HOME%\*) do echo Setup PATH for %%~ni  

:end
