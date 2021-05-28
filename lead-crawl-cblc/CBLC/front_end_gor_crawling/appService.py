import constants
import os
import json
import requests
import threading
import pandas as pd
import validation

lock = threading.Lock()

def check_for_file_contents(filename): 
    df = pd.read_excel(filename).columns
    df = df.tolist()
    print('header------------->', df)
    return validation.validate(df)



def start(fileName, email, stage):
    data_json = {}
    data_json['file_name'] = fileName
    print(fileName+"-----------------------------------------")
    data_json['email'] = email
    data_json['stage'] = stage
    output = requests.post("http://localhost:9080/CBLC/uploadnewfile", json=data_json)  #call satyas api

def saveFileToLocal(uploaded_file):

    response = False
    responseDict = dict()

    reason = constants.ALL_CONDITIONS_CORRECT

    filename = uploaded_file.filename
    filename_extension = filename.split('.')[-1]
    filename_without_extension = filename.split('.')[0]
    filename_without_extension = filename_without_extension.replace(" ", "-")
    filename_without_extension = filename_without_extension.replace("_", "-")

    if filename != '' and filename_extension in constants.FILE_EXTENSION:
        if not os.path.exists(os.getcwd() + constants.SLASH + constants.FILES_DIRECTORY):
            os.mkdir(constants.FILES_DIRECTORY)
        destinationToSaveFile = os.path.join(
            os.getcwd(), constants.FILES_DIRECTORY+constants.SLASH+filename)
        uploaded_file.save(destinationToSaveFile)

        validation_status = check_for_file_contents(destinationToSaveFile)

        if not validation_status:
            reason = constants.WRONG_HEADER
        
    if not filename_extension in constants.FILE_EXTENSION:
        reason = constants.WRONG_FILE_EXTENSION

    if filename=='':
        reason=constants.EMPTY_FILENAME
                
    responseDict['filename'] = filename
    responseDict['reason'] = reason

    return responseDict

