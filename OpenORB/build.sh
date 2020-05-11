#!/bin/sh
#
# $Id: build.sh,v 1.24 2004/07/23 06:42:22 mrumpf Exp $
#

#
# Set the TCOO_HOME variable
#
if [ -z "$TCOO_HOME" ]
then
  echo Warning: TCOO_HOME not set! Defaulting to parent directory.
  TCOO_HOME=..
  export TCOO_HOME
fi

MODULE_NAME=OpenORB


#
# Set up the environment
#
if [ -z "$JAVA_HOME" ] ; then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi
JAVA=$JAVA_HOME/bin/java


#
# Set the library path
#
UNAME=`uname`
TCOO_LIB_PATH=${TCOO_HOME}/tools/lib
case "${UNAME}" in
   AIX)
      if test -z "${LIBPATH}"
      then
         LIBPATH=$TWISTER_SLPATH:$TCOO_LIB_PATH
      else
         LIBPATH=$TWISTER_SLPATH:$TCOO_LIB_PATH:$LIBPATH
      fi
      export LIBPATH
      ;;
   Linux|SunOS)
      if [ -z "${LD_LIBRARY_PATH}" ]; then
         LD_LIBRARY_PATH=$TWISTER_SLPATH:$TCOO_LIB_PATH
      else
         LD_LIBRARY_PATH=$TWISTER_SLPATH:$TCOO_LIB_PATH:$LD_LIBRARY_PATH
      fi
      export LD_LIBRARY_PATH
      ;;
   *)
      dump "Operating system '${UNAME}' not supported! Library path not set!"
      ;;
esac


#
# Execute the build tool passing the build.xml file
#
VM_ARGS="-Dopenorb.module.name=$MODULE_NAME -Dopenorb.home.path=$TCOO_HOME"
$JAVA $VM_ARGS -jar ../tools/build.jar "$@"

