# $Id: find_combo.py,v 1.1 2003/07/10 21:40:31 ravip Exp $

# find combinations with the specified features and the minimum number
# or other features.

import sys
import combo

def names_to_feature_ids(names):
    ids=[]
    for name in names:
        found=0
        for i in range (1, len(combo.feature_list)):
            if combo.feature_list[i]==name:
                ids.append(i)
                found=1
                break
        if not found:
            print 'Warning: Couldn\'t find feature',name
    return ids

def contains_ids(combination, ids):
    for id in ids:
        if id not in combination:
            return 0
    return 1

def find_smallest_combos(ids):
    matches=[]
    matchlen=65000

    for i in range(0,len(combo.combo_list)):
        c=combo.combo_list[i]
        if contains_ids(c, ids):
            if len(c)<matchlen:
                matchlen=len(c)
                matches=[]
            if len(c)==matchlen:
                matches.append(i)

    return matches

def print_combos(combos):
    for c in combos:
        print c,'->',
        for f in combo.combo_list[c]:
            print combo.feature_list[f],


if __name__ == "__main__":
    ids=names_to_feature_ids(sys.argv[1:])
    matches=find_smallest_combos(ids)
    if len(matches)>0:
        print_combos(matches)
    else:
        print 'No matches!'
