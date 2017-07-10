import re, os
headers = map(lambda x :
                  os.path.join(os.path.dirname(os.path.realpath(__file__)), x), 
              ['DRMLib.h', 'drmalgorithm.h']
            )

pat = re.compile("EBOOK_DRM_API\s+(?P<type>\w+)\s+(?P<name>\w+)\s*\((?P<arguments>[^\)]+)\)")

names = []
import sys
def_file = open(sys.argv[1], "wt")
print >>def_file,  "EXPORTS"

for i in headers:
    f = open(i, "rt")
    buf = f.read()
    result = pat.findall(buf)
    for i in result:
        print >>def_file, "\t", i[1]
def_file.close()

