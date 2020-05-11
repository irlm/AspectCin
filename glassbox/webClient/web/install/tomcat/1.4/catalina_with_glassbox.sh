#!/bin/sh
#

echo "...Starting @TARGET_SYSTEM@ with Glassbox on JDK 1.4..."

export GLASSBOX_BIN_DIR=@BIN_DIR@
export GLASSBOX_LIB_DIR=@LIB_DIR@
export GLASSBOX_HOME=@GLASSBOX_HOME@

export @JAVA_OPTS@="$@JAVA_OPTS@ -Xbootclasspath/p:$GLASSBOX_LIB_DIR/java14Adapter.jar -Xbootclasspath/a:$GLASSBOX_LIB_DIR/createJavaAdapter.jar:$GLASSBOX_LIB_DIR/aspectj14Adapter.jar:$GLASSBOX_LIB_DIR/aspectjweaver.jar:$GLASSBOX_LIB_DIR/glassboxMonitor.jar:$GLASSBOX_LIB_DIR/aspectjrt.jar -Xmx509m -Daspectwerkz.classloader.preprocessor=org.aspectj.ext.ltw13.ClassPreProcessorAdapter -Djava.security.policy=$GLASSBOX_BIN_DIR/java.policy -Dglassbox.install.dir=$GLASSBOX_HOME -Djava.rmi.server.useCodebaseOnly=true"

@BIN_DIR@/catalina.sh $*
