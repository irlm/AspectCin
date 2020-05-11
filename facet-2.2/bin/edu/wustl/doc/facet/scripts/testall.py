#
# $Id: testall.py,v 1.1 2003/07/10 21:40:32 ravip Exp $
#

# Build and test all possible configurations of the event channel and
# log results.

import sys
import os
import string
import tempfile
import time
import random

import combo
import popular_subsets

testall_logfile = 'testall.log'
facetprop_name = 'facet.properties'
facetrun_name = time.strftime('%m%d%y',time.localtime(time.time()))

banner_msg = """

===========================================================================

%s
===========================================================================

"""

def print_banner(logfile, message):
    fp = open(logfile, 'a')
    fp.write(banner_msg % message)
    fp.close()

def create_event_properties(combination):
    s=''

    for i in combination:
        shortname = combo.feature_list[i]
        if shortname != '':
            s += shortname + '=yes\n'

    return s

total_tested=0
total_errors=0
def testone(combo_number, combination):
    global total_tested, total_errors
    print "Building %d, Total tested %d, Total failed %d." % (combo_number,total_tested, total_errors)

    config = create_event_properties(combination)

    print_banner(testall_logfile, config)

    fp = open(facetprop_name, 'w')
    fp.write(config)
    fp.close()

    tmplog = tempfile.mktemp()

    if 1:
        total_tested = total_tested + 1

        os.system("ant -Dfacet.run.name=%s -Dfacet.run.config=%d -emacs -logfile %s testall.everything.checkfirst" % (facetrun_name,combo_number,tmplog))

        # cat the log to the main log
        fd = open(tmplog, 'r')
        msgs = fd.read()
        fd.close()

        if (string.find(msgs, 'BUILD FAILED') != -1):
            print 'Combination %d failed!' % combo_number
            total_errors = total_errors + 1

        fd = open(testall_logfile, 'a')
        fd.write(msgs)
        fd.close()

        os.remove(tmplog)


def clear_logfile(logfile):
    fp = open(logfile, 'w')
    fp.close()

def scan_logfile(logfile):
    fp = open(logfile, 'r')

    error_flag = 0

    while (1):
        line = fp.readline()
        if line == '':
            break

        if (string.find(line, 'BUILD FAILED') != -1):
            error_flag = error_flag + 1

    if error_flag > 0:
        print 'FOUND %d ERRORS!' % error_flag
    else:
        print 'ALL TESTS RAN SUCCESSFULLY!'

    fp.close()

def get_run_order():
    run_order=range(0,len(combo.combo_list))

    popular=popular_subsets.get_popular_subsets()
    popular.sort()
    popular.reverse()
    for subset in popular:
        del run_order[subset]

    random.shuffle(run_order)
    return popular+run_order

def testall(args):
    # if arguments were specified, only retest combinations with those
    # features.

    retest_feature=0
    retest_combos=None
    skip_feature=0

    if len(args)>2:
        if args[1]=='--feature':
            for i in range(0,len(combo.feature_list)):
                if args[2]==combo.feature_list[i]:
                    retest_feature=i
        if args[1]=='--combo':
            retest_combos=map(int,args[2:])
        if args[1]=='--skip':
            for i in range(0,len(combo.feature_list)):
                if args[2]==combo.feature_list[i]:
                    skip_feature=i


    if retest_feature!=0:
        print 'Only retesting feature: ',retest_feature
    if retest_combos!=None:
        print 'Retesting combinations: ',retest_combos
    if skip_feature!=0:
        print 'Skipping feature: ',skip_feature

    # Hide the facet.properties file to avoid any user properties
    if os.access(facetprop_name, os.F_OK):
        os.rename(facetprop_name,facetprop_name + '.hide')

    try:
        clear_logfile(testall_logfile)

        run_order=retest_combos
        if retest_combos==None:
            run_order=get_run_order()
        numleft=len(run_order)
        print 'Testing %d/%d combinations...' % (numleft, len(combo.combo_list))
        for i in run_order:
            if ((retest_feature==0 or retest_feature in combo.combo_list[i])
                and (skip_feature==0 or not skip_feature in combo.combo_list[i])):
                testone(i, combo.combo_list[i])
            else:
                print '--> Skipped combination: ',i

            numleft=numleft-1
            print '----------> %d left!' % numleft

        scan_logfile(testall_logfile)
    finally:
        if os.access(facetprop_name, os.F_OK):
            os.remove(facetprop_name)
        os.rename(facetprop_name+'.hide',facetprop_name)


if __name__ == "__main__":

    testall(sys.argv)
