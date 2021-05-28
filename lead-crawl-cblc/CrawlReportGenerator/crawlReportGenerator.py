import csv
import datetime
import json
import logging
import os
import shutil
from zipfile import ZipFile

import boto3
from dateutil.tz import tzutc

import config

logging.basicConfig(filename='app.log', filemode='w', format='%(name)s - %(levelname)s - %(message)s')

FILE_PATH = config.REPORT_FILEPATH
startDate = datetime.datetime.now() - datetime.timedelta(days=1)
startDay = startDate.day
startMonth = startDate.month
startYear = startDate.year

endDate = datetime.datetime.now()
endDay = endDate.day
endMonth = endDate.month
endYear = endDate.year


def write(fileName, listOfDetails):
    """
    write the logging details in the csv file
    :param fileName:
    :param listOfDetails:
    :return: None
    """
    with open(fileName, config.APPEND_PLUS) as csvFile:
        csvwriter = csv.writer(csvFile)
        csvwriter.writerow(listOfDetails)


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


def getCount(files):
    """get count of the files"""
    c = 0
    for file in files:
        if config.MHTML_EXTENSION in file or config.HTML_EXTENSION in file or config.CAP_HTML_EXTENSION in file:
            c += 1
    return c


def get_all_input_json():
    """
    Get a list of all keys in an S3 bucket.
    :param s3_path: Path of S3 dir.
    """
    s3 = boto3.client('s3')
    keys = []
    total = 0
    kwargs = {'Bucket': config.AUTOMI_LOCAL_LAPTOPS_BUCKET, 'Prefix': config.SNURL_PREFIX}
    resp = None
    while True:
        try:
            resp = s3.list_objects_v2(**kwargs)
            for obj in resp['Contents']:
                date = obj['LastModified'] + datetime.timedelta(hours=5, minutes=30)
                if (date >= datetime.datetime(startYear, startMonth, startDay, 0, 0, 0,
                                              tzinfo=tzutc()) and date <= datetime.datetime(endYear, endMonth, endDay,
                                                                                            0, 0,
                                                                                            0,
                                                                                            tzinfo=tzutc())):
                    filepath = obj['Key']
                    logging.info(filepath)
                    if filepath:
                        s3.download_file(config.AUTOMI_LOCAL_LAPTOPS_BUCKET, filepath, config.CHECK_FILE)
                        with open(config.CHECK_FILE) as f:
                            data = json.load(f)
                            total += len(data['PROFILE_URL'])
        except Exception as e:
            logging.error("Exception occurred while listing and downloading...", e)
        try:
            kwargs['ContinuationToken'] = resp['NextContinuationToken']
        except KeyError:
            break
    return total


def get_all_company_keys():
    """
    Get a list of all keys in an S3 bucket.
    :param s3_path: Path of S3 dir.
    """
    s3 = boto3.client('s3')
    keys = []
    kwargs = {'Bucket': config.BUCKET_NAME, 'Prefix': config.COMPANY_JSON_PREFIX}
    resp = None
    while True:
        try:
            resp = s3.list_objects_v2(**kwargs)
            for obj in resp['Contents']:
                date = obj['LastModified'] + datetime.timedelta(hours=5, minutes=30)
                if (date >= datetime.datetime(startYear, startMonth, startDay, 0, 0, 0,
                                              tzinfo=tzutc()) and date <= datetime.datetime(endYear, endMonth, endDay,
                                                                                            0, 0,
                                                                                            0,
                                                                                            tzinfo=tzutc())):
                    k = obj['Key']
                    s1 = k.replace(config.COMPANY_JSON_PREFIX, '')
                    keys.append(s1)
        except Exception as e:
            logging.error("Exception occurred while listing and downloading...", e)
        try:
            kwargs['ContinuationToken'] = resp['NextContinuationToken']
        except KeyError:
            break
    return keys


def getEmailGenCount():
    s3 = boto3.client('s3')
    filepath = None
    emailsList = []
    total = 0
    l = []
    kwargs = {'Bucket': config.AUTOMI_CRAWLER_LAPTOPS_PROD_BUCKET, 'Prefix': config.LINKEDIN_ESPROFILE_PREFIX}
    resp = None
    while True:
        try:
            resp = s3.list_objects_v2(**kwargs)
            for obj in resp['Contents']:
                date = obj['LastModified'] + datetime.timedelta(hours=5, minutes=30)
                if (date >= datetime.datetime(startYear, startMonth, startDay, 0, 0, 0,
                                              tzinfo=tzutc()) and date <= datetime.datetime(endYear, endMonth, endDay,
                                                                                            0, 0,
                                                                                            0,
                                                                                            tzinfo=tzutc())):
                    filepath = obj['Key']
                    logging.info(filepath)
                    if filepath:
                        s3.download_file(config.AUTOMI_CRAWLER_LAPTOPS_PROD_BUCKET, filepath, config.CHECK_FILE)
                        with open(config.CHECK_FILE) as f:
                            data = json.load(f)
                            total += len(data)
                        for d in data:
                            emailId = d['email_id']
                            if (None != emailId):
                                emailsList.append(emailId)
        except Exception as e:
            logging.error("Exception occurred while listing and downloading...", e)
        try:
            kwargs['ContinuationToken'] = resp['NextContinuationToken']
        except KeyError:
            break
    l.append(total)
    l.append(len(emailsList))
    return l


def get_total_processed():
    """
    Get a list of all keys in an S3 bucket.
    :param s3_path: Path of S3 dir.
    """
    s3 = boto3.client('s3')
    keys = []
    total = 0
    kwargs = {'Bucket': config.BUCKET_NAME, 'Prefix': config.POST_PROCESSED_PREFIX}
    resp = None
    while True:
        try:
            resp = s3.list_objects_v2(**kwargs)
            for obj in resp['Contents']:
                date = obj['LastModified'] + datetime.timedelta(hours=5, minutes=30)
                if (date >= datetime.datetime(startYear, startMonth, startDay, 0, 0, 0,
                                              tzinfo=tzutc()) and date <= datetime.datetime(endYear, endMonth, endDay,
                                                                                            0, 0,
                                                                                            0,
                                                                                            tzinfo=tzutc())):
                    filepath = obj['Key']
                    logging.info(filepath)
                    if filepath:
                        s3.download_file(config.BUCKET_NAME, filepath,
                                         filepath.replace(config.POST_PROCESSED_PREFIX, ""))
                        with ZipFile(filepath.replace(config.POST_PROCESSED_PREFIX, ""), 'r') as zip:
                            zip.extractall(filepath)
                        files = getListOfFiles(filepath)
                        total += getCount(files)
                        print(total)
                        os.remove(filepath.replace(config.POST_PROCESSED_PREFIX, ""))
                        shutil.rmtree(filepath)
        except Exception as e:
            logging.error("Exception occurred while listing and downloading...", e)
        try:
            kwargs['ContinuationToken'] = resp['NextContinuationToken']
        except KeyError:
            break
    return total


def get_all_crawled():
    """
    Get a list of all keys in an S3 bucket.
    :param s3_path: Path of S3 dir.
    """
    s3 = boto3.client('s3')
    keys = []
    total = 0
    kwargs = {'Bucket': config.BUCKET_NAME, 'Prefix': config.PROCESSED_PREFIX}
    resp = None
    while True:
        try:
            resp = s3.list_objects_v2(**kwargs)
            for obj in resp['Contents']:
                date = obj['LastModified'] + datetime.timedelta(hours=5, minutes=30)
                if (date >= datetime.datetime(startYear, startMonth, startDay, 0, 0, 0,
                                              tzinfo=tzutc()) and date <= datetime.datetime(endYear, endMonth, endDay,
                                                                                            0, 0,
                                                                                            0,
                                                                                            tzinfo=tzutc())):
                    filepath = obj['Key']
                    logging.info(filepath)
                    if filepath:
                        s3.download_file(config.BUCKET_NAME, filepath, config.CHECK_FILE)
                        with open(config.CHECK_FILE) as f:
                            data = json.load(f)
                            total += len(data['PROFILE_URL'])
        except Exception as e:
            logging.error("Exception occurred while listing and downloading...", e)
        try:
            kwargs['ContinuationToken'] = resp['NextContinuationToken']
        except KeyError:
            break
    return total


Heading = ['DATE', "INPUT PROFILES", 'CRAWLED', 'POST-PROCESSED', 'COMPANY-CRAWL', 'TOTAL FOR EMAIL-GEN',
           'VALID EMAIL-GEN']
write(FILE_PATH, Heading)

details = []
companies = get_all_company_keys()
emails = getEmailGenCount()
details.append(datetime.datetime.now() - datetime.timedelta(days=1))
details.append(get_all_input_json())
total_crawled = get_all_crawled()
post_processed = get_total_processed()
details.append(total_crawled)
details.append(post_processed)
details.append(len(companies))
details.append(emails[0])
details.append(emails[1])
print(details)
write(FILE_PATH, details)
os.remove(config.CHECK_FILE)
