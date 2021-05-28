import os
from os import path
from glob import glob
import constants
import shutil
import re
import slackAlert


def deleteZip(requestId, filepath=None):
    '''
    deletes original zip file present in the directory after successfull upload
    :return boolean

    '''
    filePathToDelete = None
    if filepath is None:
        filepath = glob(constants.ALL_ZIP_FILES)
        if len(filepath) == 0:
            print('empty dir "./*zip"')
            return False
        for i in filepath:
            if re.search(str(requestId), i):
                filePathToDelete = i
                break
        #filePathToDelete = filepath[0]
    if path.isfile(filePathToDelete):
        os.remove(filePathToDelete)
    return True


def deleteAllMergedJson(requestId, filepath=None):
    '''
    removes all profile JSON files from the profile_json directory after successfull upload
    :return boolean

    '''
    if filepath is None:
        filepath = glob(constants.MERGING_JSON_DATA + constants.SLASH + constants.LOCAL_SOURCE_LINKEDIN
                        + str(requestId)+constants.UNDERSCORE + constants.ALL_JSON_FILES)
        if len(filepath) != 0:
            for file in filepath:
                if re.search(str(requestId), file):
                    os.remove(file)
            print('all files in merged_json are removed')
            return True
        else:
            print('error in removing merged_json files')
            return False


def deleteAllProfileJson(requestId, filepath=None):
    '''
    removes all profile JSON files from the profile_json directory after successfull upload
    :return boolean

    '''
    if filepath is None:
        filepath = glob(constants.PROFILE_JSON_DIRECTORY +
                        constants.ALL_JSON_FILES)
        if len(filepath) != 0:
            for file in filepath:
                if re.search(str(requestId), file):
                    os.remove(file)
            print('all files in profile_json are removed')
            return True
        else:
            print('error in removing files')
            return False


def deleteAllProfileHTML(requestId, filepath=None):
    '''
    removes all profile HTML files from the profile_html directory after successfull upload
    :return boolean

    '''

    filePathToCheck = constants.PROFILE_HTML_DIRECTORY + \
        constants.SLASH + str(requestId)
    filepath = os.listdir(filePathToCheck)
    print(len(filepath))

    try:
        # print(filepath)
        if len(filepath) != 0:
            for file in filepath:
                if os.path.isfile(file):
                    os.remove(file)
            print(
                f'all files in ProfileHtml_{str(requestId)} directory are removed')
            profileFolderPath = constants.PROFILE_HTML_DIRECTORY + \
                constants.SLASH + str(requestId)
            if path.exists(profileFolderPath):
                shutil.rmtree(os.path.join(os.getcwd(), profileFolderPath))
                print('removed folder: '+constants.PROFILE +
                      constants.UNDERSCORE + str(requestId))

    except OSError as error:
        print(error)
        print("File path can not be removed")


def deleteAllCompanyHTML(requestId, filepath=None):
    '''
    removes all company HTML files from the company_html directory 
    :return boolean

    '''
    if filepath is None:
        directoryPath = constants.COMPANY_HTML_DIRECTORY + constants.SLASH + constants.COMPANY + constants.UNDERSCORE + \
            str(requestId)
        filepath = glob(constants.COMPANY_HTML_DIRECTORY + constants.SLASH + constants.COMPANY + constants.UNDERSCORE +
                        str(requestId) + constants.SLASH + constants.ALL_TYPE_HTML_FILES)

        try:

            if len(filepath) > 0:
                for file in filepath:
                    os.remove(file)
                print('all files in company_html are removed')

                # return True
            else:
                print('error in removing files: no. of files: ' +
                      str(len(filepath)))
                # return False

            if path.isdir(directoryPath):
                print('company_path: ' + directoryPath)
                shutil.rmtree(directoryPath)

        except OSError as error:
            print(error)
            print("File path can not be removed")


def deleteCompanyJsonForRequestId(requestId):
    folderpath = constants.COMPANY_JSON_DIRECTORY + constants.SLASH + constants.COMPANY + \
        constants.UNDERSCORE+str(requestId)

    filepath = glob(constants.COMPANY_JSON_DIRECTORY + constants.SLASH + constants.COMPANY +
                    constants.UNDERSCORE + str(requestId) + constants.SLASH + constants.ALL_JSON_FILES)
    # print(filepath, folderpath, len(filepath))

    if filepath != None:

        if len(filepath) == 0:
            # print(f'No file to delete in company_json/company_{requestId}')
            shutil.rmtree(folderpath)
            return False
        else:
            for file in filepath:
                if path.isfile(file):
                    os.remove(file)
                    # print(file, ' in Company json is removed')
            shutil.rmtree(folderpath)
            return True


def deleteFinalJson(requestId, filepath=None):
    '''
    removes the final json file after uploading it to S3
    :return boolean

    '''
    fileToCheck = ''
    if filepath is None:
        filepath = glob(constants.FINAL_JSON_DIRECTORY + constants.SLASH + constants.LOCAL_SOURCE_LINKEDIN +
                        str(requestId) + constants.ALL_JSON_FILES)
        if len(filepath) == 0:
            print('empty dir "./*json"')
            return False
        files = filepath[0]
        for file in filepath:
            if re.search(str(requestId), file):
                fileToCheck = file
                break

    if path.isfile(fileToCheck):
        os.remove(fileToCheck)
        print('all files in Final json are removed')
        return True
    else:
        return False


def deleteAllFilesFromDirectory(pathToCheck, files):
    if len(files) > 0:
        print('len is: ', len(files), 'files: ', files)
        for file in files:
            print(pathToCheck + constants.SLASH + file)
            if path.isdir(pathToCheck + constants.SLASH + file):
                shutil.rmtree(pathToCheck + constants.SLASH + file)
            elif path.isfile(pathToCheck + constants.SLASH + file):
                os.remove(pathToCheck + constants.SLASH + file)
        #print("all unneccessary files are removed")
        return True
    else:
        #print('nothing to delete')
        return False

