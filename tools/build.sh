#!/bin/sh
#
# $Id: build.sh,v 1.14 2003/03/22 22:36:00 shawnboyce Exp $
#

MODULE_NAME=tools

if [ -z "$TCOO_HOME" ]
then
  echo Warning: TCOO_HOME not set! Defaulting to parent directory.
  TCOO_HOME=..
  export TCOO_HOME
fi

if [ ! -f $TCOO_HOME/tools/buildModule.sh ]
then
  echo Error: Could not find "$TCOO_HOME/tools/buildModule.sh".
  echo        The tools source distribution is not located in correct directory.
else
  $TCOO_HOME/tools/buildModule.sh $MODULE_NAME $@
fi

