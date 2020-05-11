#!/bin/csh

# This script sets up your CLASSPATH correctly so that you can
# just do an ant after sourcing this

setenv CLASSPATH `pwd`/classes

foreach p (docant docaspectj_1.1 doclog4j docjunit docjacorb docjikes)
  pkgadd $p
end

# And now we do some hackery to get rid of the references to the
# JDK jar files since GCJ does not like them

echo $CLASSPATH | sed -e 's/\/docpkg\/java_1.4.0\/jre\/lib\/rt\.jar\://' > tmp
setenv CLASSPATH `cat tmp`
/bin/rm -f tmp
