#!/bin/csh

# This script sets up your CLASSPATH correctly so that you can
# just do an ant after sourcing this

setenv CLASSPATH `pwd`/classes

foreach p(docant docaspectj_1.1 docjunit doclog4j docjacorb docjopt docjikes tjvm)
  pkgadd $p
end
