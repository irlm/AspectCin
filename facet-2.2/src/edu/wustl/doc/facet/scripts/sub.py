import sys

src=""
work_queue=[]
applied_aspects={}
not_done_flag=0

def do_substitutions(srcfilename, destfilename, subfilelist):
    global src
    global work_queue
    global applied_aspects
    global not_done_flag

    # read everything
    srcfile=open(srcfilename)
    src=srcfile.read()
    srcfile.close()

    # perform the substitutions
    work_queue=subfilelist

    while (len(work_queue)!=0):
        work_item=work_queue.pop(0)

        not_done_flag=0
        # print "Applying ",work_item
        execfile(work_item)
        if (not_done_flag!=0):
            work_queue.append(work_item)

    # write the file back
    destfile=open(destfilename,'w')
    destfile.write(src)
    destfile.close()


if __name__ == "__main__":

    # load in substitution files
    if (len(sys.argv)<3):
        print "Usage: sub.py srcfile destfile [substitution files...]"
        sys.exit(1)

    do_substitutions(sys.argv[1], sys.argv[2], sys.argv[3:])
