import os
import constants
from zipfile import ZipFile


def extract_zip_file(requestId):
    '''
    it is used to extract zip file at specified directory
    param: fileDirectory - where it needs to be extracted
    '''
    fileDirectory = str(requestId) + constants.ZIP_EXTENSION
    extracted_file = ZipFile(os.getcwd() + constants.SLASH + fileDirectory, constants.READ_MODE)
    extracted_file.extractall(constants.ZIP_EXTRACT_DIRECTORY+str(requestId))
