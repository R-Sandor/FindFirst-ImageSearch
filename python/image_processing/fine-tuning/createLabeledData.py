import os
import csv
with open('pilot_data.csv', 'w', newline='') as csvfile:
    filewriter = csv.writer(csvfile, delimiter=',',
                            quotechar='|', quoting=csv.QUOTE_MINIMAL)
    filewriter.writerow(['filename', 'filepath', 'label'])
    
    for dir in os.walk("../../../data/SciFig-pilot"): 
        if dir[0] == "../../../data/SciFig-pilot/png": 
            continue
        elif dir[0] == "../../../data/SciFig-pilot/metadata":
            continue
        elif dir[0] == "../../../data/SciFig-pilot":
            continue
        loc = dir[0]
        idx = loc.rfind("/")
        label = loc[idx+1:]
        files = dir[2]
        for file in files: 
            fullpath = loc + "/" + file
            filewriter.writerow([file, fullpath, label])



