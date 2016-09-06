import sys


current_u = None
current_p = None
current_c = None


def flush_joins():
    global current_u, current_p, current_c
    if not current_u:
        return
    for p in current_p:
        for c in current_c:
            print current_u,"\t",p,"\t",c
    current_p = []
    current_c = []


def reducer(key, value):
    global current_u, current_p, current_c
    if key != current_u:
        flush_joins()
        current_u = key
    splits = value.split(" ", 1)
    if splits[0] == 'p':
        current_p.append(splits[1])
    elif splits[0] == 'c':
        current_c.append(splits[1])



if __name__ == '__main__':
    global current_u, current_p, current_c
    current_u = None
    current_p = []
    current_c = []
    line = sys.stdin.readline()
    try:
        while line:
            splits = line.strip().split('\t', 1)
            reducer(splits[0], splits[1])
            line = sys.stdin.readline()
    except:
        pass
    flush_joins()
