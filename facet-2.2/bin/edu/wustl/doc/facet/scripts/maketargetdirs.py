import sys
import os
import os.path

def make_target_dirs(target_list):
    for target in target_list:
        targetdir=os.path.dirname(target)
        if (not os.path.exists(targetdir)):
            os.makedirs(targetdir);

if __name__ == "__main__":
    make_target_dirs(sys.argv)
    sys.exit(0)
