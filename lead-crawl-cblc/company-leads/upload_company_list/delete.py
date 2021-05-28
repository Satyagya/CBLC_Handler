import os
import constants

def deleteDownloadedFile(filename):
    filepath = constants.FILES_DIRECTORY+constants.SLASH+filename
    try:
        if os.path.isfile(filepath):
            os.remove(filepath)
    except Exception as e:
        print(e)
