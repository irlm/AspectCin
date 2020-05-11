echo "...Starting @TARGET_SYSTEM@ with Glassbox .."

SETLOCAL

set GLASSBOX_BIN_DIR=@BIN_DIR@
set GLASSBOX_LIB_DIR=@LIB_DIR@
set GLASSBOX_HOME=@GLASSBOX_HOME@

set @JAVA_OPTS@=%@JAVA_OPTS@% "-javaagent:%GLASSBOX_LIB_DIR%\aspectjweaver.jar" -Daj.weaving.verbose=false "-Dglassbox.install.dir=%GLASSBOX_HOME%"

call "@BIN_DIR@\catalina.bat" %*
ENDLOCAL