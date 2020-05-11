import os, sys, re
from stat import *

classes_dir='/home/hokey/ravip/RTEvent/facet/classes'
facet_offset='edu/wustl/doc/facet'

exclude_regex=0
filesum=0

def walktree(dir, callback):
    '''recursively descend the directory rooted at dir,
       calling the callback function for each regular file'''

    for f in os.listdir(dir):
        pathname = '%s/%s' % (dir, f)
        stat = os.stat(pathname)
        mode = stat[ST_MODE]
        if S_ISDIR(mode):
            # It's a directory, recurse into it
            walktree(pathname, callback)
        elif S_ISREG(mode):
            # It's a file, call the callback function
            callback(pathname, stat)
        else:
            # Unknown file type, print a message
            print 'Skipping %s' % pathname

def visitfile(file, stat):
    global filesum

    # exclude test files
    if (exclude_regex.search(file) == None):
        print 'visiting', file, ' size = ', stat[ST_SIZE]
        filesum = filesum + stat[ST_SIZE]

def sum_class_files():
    global exclude_regex
    exclude_regex=re.compile('[Tt]est')
    walktree('%s/%s' % (classes_dir,facet_offset), visitfile)
    print 'Total class file size = ', filesum

if __name__ == '__main__':
    sum_class_files()
