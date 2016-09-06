import sys

#def logger(msg):
#    with open('/root/hadoop/output/debug.log') as f:
#         print(msg, file=f)

def mapper(line):
#    logger(line)
    line = line.strip()
    splits = line.split(",")
    if len(splits) == 5:
        print splits[2],"\t","p"," ",splits[1]
    elif len(splits) == 4:
        print splits[0],"\t","c"," ",splits[3]
    else:
        pass




if __name__ == '__main__':
    line = sys.stdin.readline()
    try:
        while line:            
            mapper(line)
            line = sys.stdin.readline()
    except "end of file":
        pass

