#!/bin/sh
#
# $Id: buildModule.sh,v 1.3 2003/03/18 04:26:53 shawnboyce Exp $
#

if [ -z "$TCOO_HOME" ]
then
  echo Warning: TCOO_HOME not set! Defaulting to parent directory.
  TCOO_HOME=..
  export TCOO_HOME
fi

MODULE_NAME=$1
shift

ARGS="$1"
shift
until [ -z "$1" ]
do
   ARGS="$ARGS $1"
   shift
done

echo Building module: \"$MODULE_NAME\" located in: \"$TCOO_HOME/$MODULE_NAME\"


if [ -z "$JAVA_HOME" ] ; then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi

if [ ! -f "$JAVA_HOME/bin/java" ]
then
  echo Error: Could not find \"$JAVA_HOME/bin/java\".
  echo        Please set JAVA_HOME to installed Java SDK.
  exit 2
fi

JAVA=$JAVA_HOME/bin/java
$JAVA -Dopenorb.module.name=$MODULE_NAME -Dopenorb.home.path=$TCOO_HOME -jar $TCOO_HOME/tools/build.jar $ARGS
if [ $? -ne 0 ]
then
  echo Error: Failed to build \"$MODULE_NAME\" with arguments {$ARGS} 
fi

