import logging
import os
import boto3
import s3Operations
import config
import constants
from glob import glob


def remove(string):
    return string.replace(" ", "")


def obj_last_modified(myobj):
    """
    Gets the last modified date of the file
    :param myobj:
    :return: Last Modified timestamp of file
    """
    return myobj[constants.LAST_MODIFIED]


def findCompanyWithName(companyName):
    """
    Gets the file with the least timestamp
    :return: file with the least timestamp
    """
    s3client = boto3.client('s3')

    try:
        response = s3client.list_objects(
            Bucket=config.BUCKET_NAME, Prefix=config.COMPANY_JSON_CHECK+companyName)
        # print("response for companyName: ",companyName, response)
        l = []
        if ('Contents' in response):
            for key in response['Contents']:
                l.append(key)

        sortedObjects = sorted(l, key=obj_last_modified, reverse=False)
        if len(sortedObjects) > 0:
            # print(len(sortedObjects), sortedObjects[0]['Key'])
            return sortedObjects[0]['Key']
        else:
            return None
        # print('resonpnse is: ', response)
    except Exception as e:
        logging.error(e)

        print(
            f'Unexpected error while listing the dir {config.BUCKET_NAME}:{config.COMPANY_JSON_CHECK}')
        return None

    return None


def fetchCompanyJson(companyName, requestId):
    """
    fetches companyJSON and returns json data as result
    :param companyName:
    :return: json data
    """

    requestId = str(requestId)
    if ' ' in companyName:
        companyName = remove(companyName)
    filePath = findCompanyWithName(companyName)
    if (filePath):
        try:
            details = []
            pathToDownload = constants.COMPANY_JSON_DIRECTORY+constants.SLASH + constants.COMPANY + \
                constants.UNDERSCORE + requestId + constants.SLASH + \
                companyName + constants.JSON_EXTENSION
            # print(pathToDownload)
            s3Operations.download_file(
                config.BUCKET_NAME, filePath, pathToDownload)

            return 1
        except Exception as e:
            logging.error(e)
            print(
                f'Unexpected error while downloading from {config.BUCKET_NAME}:{filePath}')
            raise
    else:
        print("No company with name "+companyName+" found!!")
    return 0


def findProfileJsonWithRequestId(requestId):
    """
    Gets the list of profile json with requestId
    :return: list of profile json name with required request Id
    """
    s3client = boto3.client('s3')
    try:
        profileJsonFolderPathS3 = config.JSON_UPLOAD+constants.SLASH+config.PROFILE_JSON +\
            constants.SLASH + constants.LOCAL_SOURCE_LINKEDIN + \
            str(requestId)+constants.UNDERSCORE
        response = s3client.list_objects(
            Bucket=config.BUCKET_NAME, Prefix=profileJsonFolderPathS3)
        l = []
        if ('Contents' in response):

            for key in response['Contents']:
                # print('key in response',key)
                l.append(key['Key'])

        # sortedObjects = sorted(l, key=obj_last_modified, reverse=False)
        return l
    except Exception as e:
        logging.error(e)
        print(
            f'Unexpected error while listing the dir {config.BUCKET_NAME}:{profileJsonFolderPathS3}')

    return None


def fetchProfileJson(requestId):
    """
    fetches profileJSON and downloads it in merge json data Folder
    :param requestId:
    :return: json data
    """
    filePaths = findProfileJsonWithRequestId(requestId)
    if (len(filePaths)) > 0:
        for file in filePaths:
            filename = str(file).split('/')[-1]
            try:
                details = []
                requestId = None
                s3Operations.download_file(
                    config.BUCKET_NAME, file, constants.MERGING_JSON_DATA + constants.SLASH + filename)

            except Exception as e:
                logging.error(e)
                print(
                    f'Unexpected error while downloading from {config.BUCKET_NAME}:{file}')
                raise
    else:
        print("No profile json found  with requestId "+str(requestId)+" found!!")


def findZipToCreateBatch(filename):
    s3client = boto3.client('s3')
    response = None
    try:
        response = s3client.list_objects(
            Bucket=config.BUCKET_NAME, Prefix=config.MOBILE_CRAWLED_ZIPS + filename)
        # print('resonpnse is: ', response)
        l = []
        if ('Contents' in response):

            for key in response['Contents']:
                # print(key)
                l.append(key)
        # print(l)
        sortedObjects = sorted(l, key=obj_last_modified, reverse=False)
        return sortedObjects[0]['Key']
    except Exception as e:
        logging.error(e)
        print(
            f'Unexpected error while listing the dir {config.BUCKET_NAME}:{config.MOBILE_CRAWLED_ZIPS}')

    return None


def findZipWithRequestId(requestId):
    """
    Gets the zip file to be download
    :return: zip file name to be download
    """
    s3client = boto3.client('s3')
    response = None
    try:
        response = s3client.list_objects(
            Bucket=config.BUCKET_NAME, Prefix=config.BATCH_PRESENT_FOLDER +
            constants.SLASH + str(requestId) + constants.ZIP_EXTENSION )
        # print('resonpnse is: ', response)
        l = []
        if ('Contents' in response):

            for key in response['Contents']:
                # print(key)
                l.append(key)
        # print(l)

        sortedObjects = sorted(l, key=obj_last_modified, reverse=False)
        if len(sortedObjects) > 0:
            return sortedObjects[0]['Key']

    except Exception as e:
        logging.error(e)
        print(
            f'Unexpected error while listing the dir {config.BUCKET_NAME}:{config.BATCH_PRESENT_FOLDER}')

    return None


def fetchProfileZip(requestId):
    """
    fetches zipFile for post processing
    :param zipname:
    
    """
    filePath = findZipWithRequestId(requestId)
    if (filePath):
        fileName = str(filePath).split(constants.SLASH)[-1]
        print(fileName)
        try:
            details = []
            requestId = None
            s3Operations.download_file(
                config.BUCKET_NAME, filePath, os.getcwd()+constants.SLASH+fileName)
            return fileName
        except Exception as e:
            logging.error(e)
            print(
                f'Unexpected error while downloading from {config.BUCKET_NAME}:{filePath}')
            return 'no file found'
    else:
        print("No zip found with requestId "+str(requestId)+" found!!")
        return 'no file found'


def findFinalJsonWithRequestId(requestId):
    """
    Gets the list of profile json with requestId
    :return: list of profile json name with required request Id
    """
    s3client = boto3.client('s3')
    try:
        profileJsonFolderPathS3 = config.JSON_UPLOAD+constants.SLASH+config.FINAL_JSON +\
            constants.SLASH + constants.LOCAL_SOURCE_LINKEDIN + str(requestId)
        response = s3client.list_objects(
            Bucket=config.BUCKET_NAME, Prefix=profileJsonFolderPathS3)
        # print('resonpnse is: ', response)
        if ('Contents' in response):
            l = []
            for key in response['Contents']:
                # print('key in response',key)
                l.append(key['Key'])

        # sortedObjects = sorted(l, key=obj_last_modified, reverse=False)
        return l
    except Exception as e:
        logging.error(e)
        print(
            f'Unexpected error while listing the dir {config.BUCKET_NAME}:{profileJsonFolderPathS3}')

    return None


def fetchFinalJson(requestId):
    """
    fetches profileJSON and downloads it in merge json data Folder
    :param listOfProfileJson:
    :return: json data
    """
    filePaths = findFinalJsonWithRequestId(requestId)
    if filePaths != None:
        if (len(filePaths)) > 0:
            for file in filePaths:
                filename = str(file).split('/')[-1]
                try:
                    details = []
                    requestId = None
                    s3Operations.download_file(
                        config.BUCKET_NAME, file, constants.MERGING_JSON_DATA+constants.SLASH +
                        filename)

                except Exception as e:
                    logging.error(e)
                    print(
                        f'Unexpected error while downloading from {config.BUCKET_NAME}:{file}')
                    raise
        else:
            print("No profile json found  with requestId " +
                  str(requestId)+" found!!")
    else:
        print("No profile json found  with requestId "+str(requestId)+" found!!")

