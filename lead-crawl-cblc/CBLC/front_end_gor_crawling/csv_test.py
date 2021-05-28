import pandas as pd
import constants
  
def convert_to_csv(filename, stage):  
    read_file = pd.read_excel (constants.BASE_PATH + filename)
    read_file = read_file.replace('"','', regex=True)
    if int(stage) == 1:
        read_file['Linkedin URL1'] = 'None'
    filename_for_csv = filename.split(".")[0]+".csv"
    read_file.to_csv (constants.BASE_PATH + filename_for_csv, index = None, header=True)

    return filename_for_csv