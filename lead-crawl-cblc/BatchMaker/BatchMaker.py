import csv
import datetime
import logging
import math
import os
import shutil
import sys
import time
import traceback
from zipfile import ZipFile

import boto3
import slack
from apscheduler.schedulers.background import BackgroundScheduler
from botocore.exceptions import ClientError

import config

BATCH_SIZE = config.BATCH_SIZE
TOKEN = config.TOKEN
CHANNEL = config.CHANNEL


def delete_object(bucket_name, object_name):
    """Delete an object from an S3 bucket

    :param bucket_name: string
    :param object_name: string
    :return: True if the referenced object was deleted, otherwise False
    """
    s3 = boto3.client('s3')
    try:
        s3.delete_object(Bucket=bucket_name, Key=object_name)
        print('Deleted')
    except ClientError as e:
        logging.error(e)
    except Exception as e:
        logging.error(e)
        print('delete failed')
        raise
    return True


def copy_object(src_bucket_name, src_object_name,
                dest_bucket_name, dest_object_name=None):
    """Copy an Amazon S3 bucket object
    :param src_bucket_name: string
    :param src_object_name: string
    :param dest_bucket_name: string. Must already exist.
    :param dest_object_name: string. If dest bucket/object exists, it is
    overwritten. Default: src_object_name
    :return: True if object was copied, otherwise False
    """
    # Construct source bucket/object parameter
    copy_source = {'Bucket': src_bucket_name, 'Key': src_object_name}
    if dest_object_name is None:
        dest_object_name = src_object_name
    # Copy the object
    s3 = boto3.client('s3')
    try:
        s3.copy_object(CopySource=copy_source, Bucket=dest_bucket_name,
                       Key=dest_object_name)
    except ClientError as e:
        logging.error(e)
        raise
    return True


def move_object(src_bucket_name, src_object_name,
                dest_bucket_name, dest_object_name=None):
    """Copy an Amazon S3 bucket object

    :param src_bucket_name: string
    :param src_object_name: string
    :param dest_bucket_name: string. Must already exist.
    :param dest_object_name: string. If dest bucket/object exists, it is
    overwritten. Default: src_object_name
    :return: True if object was copied, otherwise False
    """
    try:
        success = copy_object(src_bucket_name, src_object_name,
                              dest_bucket_name, dest_object_name)
        if success:
            delete_object(src_bucket_name, src_object_name)

        print('{} Deleted'.format(src_object_name))
    except Exception as e:
        logging.error(e)
        print(
            f'Moving failed {src_bucket_name}:{src_object_name} to {dest_bucket_name}:{dest_object_name}')
        raise


def sendNotification(message):
    client = slack.WebClient(TOKEN, timeout=30)
    client.chat_postMessage(channel=CHANNEL, text=message, username=config.SLACK_USERNAME,
                            icon_emoji=':robot_face')


def write(fileName, listOfDetails):
    """
    write the logging details in the csv file
    :param fileName:
    :param listOfDetails:
    :return: None
    """
    with open(fileName, config.APPEND_PLUS_MODE) as csvFile:
        csvwriter = csv.writer(csvFile)
        csvwriter.writerow(listOfDetails)


def getRequestIdForBatch():
    requestId = None
    try:
        with open(config.REQUEST_ID_COUNTER_FILE, "r+") as f:
            file = f.read()
            if len(file) > 0:
                file = file.splitlines()[0]
                requestId = int(file)
                f.seek(0)
                f.flush()
                f.write(str(requestId + 1))
                f.close()
        return requestId
    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)


def get_all_crawled_zip_keys():
    """
    Get a list of all keys in an S3 bucket.
    :param s3_path: Path of S3 dir.
    """
    s3 = boto3.client('s3')
    keys = []
    kwargs = {'Bucket': config.BUCKET_NAME, 'Prefix': config.CRAWLED_ZIPS_FOLDER}
    while True:
        try:
            resp = s3.list_objects_v2(**kwargs)
            for obj in resp['Contents']:
                k = obj['Key']
                keys.append(k)
        except Exception as e:
            excep = []
            excep.append("Exception occurred while getting all crawled zips")
            write(config.LOG_FILE, excep)
            print("Exception occurred....")
        try:
            kwargs['ContinuationToken'] = resp['NextContinuationToken']
        except KeyError:
            break
    return keys


def getListOfFiles(dirName):
    """
    gets all file under directory
    :param dirName:
    :return:
    """
    listOfFile = os.listdir(dirName)
    allFiles = list()
    for entry in listOfFile:
        fullPath = os.path.join(dirName, entry)
        if os.path.isdir(fullPath):
            allFiles = allFiles + getListOfFiles(fullPath)
        else:
            allFiles.append(fullPath)
    return allFiles


def unzipAndMakeBatches():
    details = []
    s3 = boto3.client('s3')
    with ZipFile(config.CHECK_ZIP, config.READ_MODE) as zip:
        zip.extractall(config.CHECK_FOLDER)
    totalFiles = len(getListOfFiles(config.CHECK_FOLDER))
    totalBatches = math.ceil(totalFiles / BATCH_SIZE)
    details.append(totalFiles)
    details.append(totalBatches)
    for i in range(totalBatches):
        try:
            os.mkdir(str(i))
            c = 0
            files = getListOfFiles(config.CHECK_FOLDER)
            print(len(files))
            for file in files:
                if (c == BATCH_SIZE):
                    break;
                if config.HTML_EXTENSION in file or config.MHTML_EXTENSION in file:
                    ls = file.split(config.SLASH)
                    shutil.move(file, str(i) + config.SLASH + ls[len(ls) - 1])
                    c += 1
            name = str(getRequestIdForBatch())
            shutil.make_archive(name, 'zip', str(i))
            zipName = name + config.ZIP_EXTENSION
            s3.upload_file(zipName, config.BUCKET_NAME, config.BATCH_CHECK + zipName)
            details.append("uploaded successfully " + str(zipName) + " at " + str(
                datetime.datetime.now() + datetime.timedelta(hours=5, minutes=30)))
            sendNotification("uploaded successfully " + zipName)
            os.remove(zipName)
            shutil.rmtree(str(i))
        except Exception as e:
            shutil.rmtree(str(i))
            sendNotification("---------exception-------")
            details.append("Exception occurred in batch maker : " + str(e))
            print("Exception occurred in batch maker : ", e)
    write(config.LOG_FILE, details)


def makeBatches():
    zipKeys = get_all_crawled_zip_keys()
    if config.CRAWLED_ZIPS_FOLDER in zipKeys:
        zipKeys.remove(config.CRAWLED_ZIPS_FOLDER)
    print(zipKeys)
    for key in zipKeys:
        try:
            details = []
            details.append("Starting batch making for " + key)
            write(config.LOG_FILE, details)
            sendNotification("Starting batch making for " + key)
            s3 = boto3.client('s3')
            s3.download_file(config.BUCKET_NAME, key, config.CHECK_ZIP)
            unzipAndMakeBatches()
            move_object(config.BUCKET_NAME, key, config.BUCKET_NAME,
                        config.BATCH_PROCESSING_COMPLETE_FOLDER + key.replace(config.CRAWLED_ZIPS_FOLDER, config.BLANK))
            os.remove(config.CHECK_ZIP)
            shutil.rmtree(config.CHECK_FOLDER)
        except Exception as e:
            os.remove(config.CHECK_ZIP)
            excep = []
            excep.append("Exception occurred for key :" + str(key) + "  " + str(e))
            write(config.LOG_FILE, excep)
            sendNotification("---------Exception-------")
            sendNotification('Exception for ' + str(key))
            sendNotification("--------------------------")
            print('Exception occurred : ', str(e))


sched = BackgroundScheduler()
sched.add_job(makeBatches, 'interval', minutes=30)
sched.start()
while True:
    time.sleep(0)
