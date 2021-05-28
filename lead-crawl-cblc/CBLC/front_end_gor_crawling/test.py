import pandas as pd
import constants
  
def convert_to_csv():  
    read_file = pd.read_excel ('/Users/mohitchhabra/Downloads/RTW-Pops-5.xlsx')
    filename = 'RTW-Pops-6.xlsx'
    read_file = read_file.replace('"','', regex=True)
    filename_for_csv = filename.split(".")[0]+".csv"
    read_file.to_csv (constants.BASE_PATH + filename_for_csv, index = None, header=True)

    

convert_to_csv()

