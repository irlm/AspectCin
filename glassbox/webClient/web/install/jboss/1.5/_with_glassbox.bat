echo "...Starting @TARGET_SYSTEM@ with Glassbox .."

SETLOCAL

set GLASSBOX_BIN_DIR=@BIN_DIR@
set GLASSBOX_LIB_DIR=@LIB_DIR@
set GLASSBOX_HOME=@GLASSBOX_HOME@

set @JAVA_OPTS@=%@JAVA_OPTS@% "-javaagent:%GLASSBOX_LIB_DIR%\aspectjweaver.jar" "-Xbootclasspath/a:%GLASSBOX_LIB_DIR%\createJavaAdapter.jar;%GLASSBOX_LIB_DIR%\aspectj14Adapter.jar;%GLASSBOX_LIB_DIR%\aspectjweaver.jar;%GLASSBOX_LIB_DIR%\glassboxMonitor.jar;%GLASSBOX_LIB_DIR%\aspectjrt.jar" -Daj.weaving.verbose=false "-Dglassbox.install.dir=%GLASSBOX_HOME%" -Xmx509m

call "@LAUNCH_COMMAND@" %*
ENDLOCAL