#
# $Id: classfilestats.py,v 1.1 2003/07/10 21:40:31 ravip Exp $
#

# Get statistics on the class files

import os
import os.path
import sys

def visit_classdir(arg, dirname, names):
    for name in names:
        pathname = dirname + '/' + name
        if os.path.isfile(pathname):
            print '%s\t%s' % (pathname, os.path.getsize(pathname))

if __name__ == "__main__":
    if (len(sys.argv) != 2):
        print "Usage: classfilestats.py path"
        sys.exit(1)

    os.chdir(sys.argv[1])
    os.path.walk('.', visit_classdir, None)
    sys.exit(0)
