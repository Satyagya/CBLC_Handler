import sys
import config
import os
import s3Operations
from glob import glob
from os import path
import botocore
from botocore.exceptions import ClientError
import boto3
import logging
from datetime import datetime
import constants
import re
import sys
import traceback
import slackAlert

formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')


def setup_logger(name, log_file, level=logging.INFO):
    """To setup as many loggers as you want"""

    handler = logging.FileHandler(log_file)
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)

    return logger


s3Logger = setup_logger(
    constants.S3LOGGER, constants.S3_PROCESSING_LOGS)



def upload_profile_json(requestId):
    '''
    Uploads profile json files in required S3 bucket

    '''
    json_paths = glob(os.getcwd() + constants.SLASH + constants.PROFILE_JSON_DIRECTORY + constants.LOCAL_SOURCE_LINKEDIN +\
        str(requestId) + constants.UNDERSCORE + constants.ALL_JSON_FILES )
    # print('all files in profile json are', json_paths)
    if len(json_paths) > 0:
        for json_path in json_paths:
            bucket_folder_name = config.JSON_UPLOAD + constants.SLASH + config.PROFILE_JSON + constants.SLASH +\
                os.path.basename(json_path)
            try:
                print("uploading:", json_path)
                s3Operations.upload_file(
                    config.BUCKET_NAME, json_path, bucket_folder_name)

            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                tb = traceback.extract_tb(e.__traceback__)
                s3Logger.error(
                    f" Error occurred while uploading profile json files for requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
                slackAlert.sendNotification(
                    f"Error occurred while uploading profile json files for requestId:: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")

                print("Upload exception")
            # os.remove(html_path)
        print("All Profile Json are uploaded")
    else:
        print('no profile json file to upload')


def upload_profile_html_s3(requestId):
    '''
    uploads profile HTML files to required S3 bucket
    params: 
    directory: directory from where the files have to be taken

    '''

    requestId = str(requestId)
    html_paths = os.listdir(constants.PROFILE_HTML_DIRECTORY+constants.SLASH+ requestId)
    basePath = constants.PROFILE_HTML_DIRECTORY + constants.SLASH + requestId + constants.SLASH
    print("uploading profile htmls",len(html_paths))
    if len(html_paths)>0:
        for html_path in html_paths:
            
            try:
                # bucket_folder_name = 'Offline-Profile-HTML/' + path.basename(html_path)
                bucket_folder_name = config.JSON_UPLOAD+constants.SLASH + \
                    config.PROFILE_HTML + constants.SLASH + os.path.basename(html_path)
                # print("uploading: ", html_path)
                s3Operations.upload_file(
                    config.BUCKET_NAME, basePath + html_path, bucket_folder_name)
                # print('uploaded: '+html_path)
            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                tb = traceback.extract_tb(e.__traceback__)
                s3Logger.error(
                    f" Error occurred while uploading html files for requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
                slackAlert.sendNotification(
                    f"Error occurred while uploading html files for requestId:: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")



        print("All Profile HTML is uploaded")
    else:
        print("no profile html file to upload")


def uploadZipFileToS3(requestId):
    '''
    uploads the zipFile to S3 bucket

    '''
    zipFile = None
    Files = glob(os.getcwd()+constants.SLASH+constants.ALL_ZIP_FILES)
    if len(Files)>0:
        # zipFile = File[0]
        for f in Files:
            if re.search(str(requestId), f):
                zipFile = f
                break

    #print(zipFile)
        bucket_folder = config.JSON_UPLOAD+constants.SLASH+config.ZIP_FILE + constants.SLASH + \
            os.path.basename(zipFile)
        try:
            s3Operations.upload_file(config.BUCKET_NAME,
                                zipFile, bucket_folder)
        except Exception as e:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            tb = traceback.extract_tb(e.__traceback__)
            s3Logger.error(
                f" Error occurred while uploading: {zipFile}, Error = {e} traceback = {tb}  error type = {exc_type}")
            slackAlert.sendNotification(
                f"Error occurred while uploading: {zipFile}, Error = {e} traceback = {tb}  error type = {exc_type}")

    else:
        print('no file found to upload')


def uploadFinalJson():
    '''
    uploads the final json file to S3 bucket

    '''

    file = glob(constants.FINAL_JSON_DIRECTORY+ constants.SLASH +constants.ALL_JSON_FILES)
    try:
        if len(file)>0:
            downloaded_processed_json = file[0]
            # bucket_folder = config.AUTOMI_LOCAL_LAPTOP_PRODUCTION+constants.SLASH +config.FINAL_JSON+ constants.SLASH + \
            #     os.path.basename(downloaded_processed_json)
            bucket_folder = config.OFFLINE_RESPONSE + constants.SLASH + os.path.basename(downloaded_processed_json)
            # print(bucket_folder)
            s3Operations.upload_file(config.AUTOMI_LOCAL_LAPTOP_PRODUCTION,
                                downloaded_processed_json, bucket_folder)
            print("uploaded final json to Offile-response bucket")
            return True
        else:
            print('no file found to upload')
            return False
    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        s3Logger.error(
            f" Error occurred while uploading: {file[0]}, Error = {e} traceback = {tb}  error type = {exc_type}")
        slackAlert.sendNotification(
            f"Error occurred while uploading: {file[0]}, Error = {e} traceback = {tb}  error type = {exc_type}")
        return False

def uploadFileObj(bucket='company-html', file_name='local_path', object_name='s3_path'):
    if object_name is None:
        object_name = file_name       
    s3 = boto3.client('s3')
    
    try: 
        s3.meta.client.upload_file(file_name, bucket, object_name)
    except Exception as e:
        print(e)

    return True




def ls_object(bucket='company-html', dir='IN', prefix_filter=None):
    try:
        if prefix_filter is None: prefix = dir #+ constants.SLASH
        else:
            prefix = dir #+ constants.SLASH + prefix_filter
        s3 = boto3.resource('s3')
        bucket = s3.Bucket(name=bucket)
        files = set()
        FilesNotFound = True
        # print(prefix)

        for obj in bucket.objects.filter(Prefix=prefix):
            # print('{0}:{1}'.format(bucket.name, obj.key))
            files.add(str(obj.key))
            FilesNotFound = False
        if FilesNotFound:
            print("ALERT", "No file in {0}/{1}".format(bucket, prefix))
        return list(files - {dir + '/'} - {dir + '/' + dir + '/'})

    except ClientError as e:
        logging.error(e)
        raise
    except Exception as e:
        logging.error(e)
        print(f'Unexpected error while listing the dir {bucket}:{dir}')
        raise


def aquaire_lock(Bucket='company-html', Object='central_data.txt', Retention={
        'Mode': 'GOVERNANCE',
        'RetainUntilDate': datetime(2020, 1, 1)},
        ):
    client = boto3.client('s3')
    response = client.put_object_retention(
    Bucket=Bucket,
    Key=Object,
    Retention={
        'Mode': 'COMPLIANCE',
        'RetainUntilDate': datetime(2020, 1, 1)
    },
    # RequestPayer='requester',
    # VersionId='string',
    # BypassGovernanceRetention=True|False,
    # ContentMD5='string'
    )
    return response


