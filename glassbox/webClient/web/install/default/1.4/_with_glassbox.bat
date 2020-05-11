echo "...Starting @TARGET_SYSTEM@ with Glassbox on a 1.4 JVM..."

SETLOCAL

set GLASSBOX_BIN_DIR=@BIN_DIR@
set GLASSBOX_LIB_DIR=@LIB_DIR@
set GLASSBOX_HOME=@GLASSBOX_HOME@

set @JAVA_OPTS@=%@JAVA_OPTS@% "-Xbootclasspath/p:%GLASSBOX_LIB_DIR%\java14Adapter.jar" "-Xbootclasspath/a:%GLASSBOX_LIB_DIR%\createJavaAdapter.jar;%GLASSBOX_LIB_DIR%\aspectj14Adapter.jar;%GLASSBOX_LIB_DIR%\aspectjweaver.jar;%GLASSBOX_LIB_DIR%\glassboxMonitor.jar" -Daspectwerkz.classloader.preprocessor=org.aspectj.ext.ltw13.ClassPreProcessorAdapter "-Dglassbox.install.dir=%GLASSBOX_HOME%" -Djava.rmi.server.useCodebaseOnly=true

call "@LAUNCH_COMMAND@" %*
ENDLOCAL