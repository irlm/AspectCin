#!/bin/sh
#

echo "...Starting @TARGET_SYSTEM@ with Glassbox .."

export GLASSBOX_BIN_DIR=@BIN_DIR@
export GLASSBOX_LIB_DIR=@LIB_DIR@
export GLASSBOX_HOME=@GLASSBOX_HOME@

export @JAVA_OPTS@="$@JAVA_OPTS@ -javaagent:$GLASSBOX_LIB_DIR/aspectjweaver.jar -Dglassbox.install.dir=$GLASSBOX_HOME -Djava.rmi.server.useCodebaseOnly=true"

@BIN_DIR@/catalina.sh $*
