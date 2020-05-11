#!/bin/sh
#

echo "...Starting @TARGET_SYSTEM@ with Glassbox .."

export GLASSBOX_BIN_DIR=@BIN_DIR@
export GLASSBOX_LIB_DIR=@LIB_DIR@
export GLASSBOX_HOME=@GLASSBOX_HOME@

export @JAVA_OPTS@="$@JAVA_OPTS@ -javaagent:$GLASSBOX_LIB_DIR/aspectjweaver.jar -Xbootclasspath/a:$GLASSBOX_LIB_DIR/createJavaAdapter.jar:$GLASSBOX_LIB_DIR/aspectj14Adapter.jar:$GLASSBOX_LIB_DIR/aspectjweaver.jar:$GLASSBOX_LIB_DIR/glassboxMonitor.jar:$GLASSBOX_LIB_DIR/aspectjrt.jar -Xmx509m -Dglassbox.install.dir=$GLASSBOX_HOME -Djava.rmi.server.useCodebaseOnly=true"

exec @LAUNCH_COMMAND@ $*
