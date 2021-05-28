import datetime
import logging
import os
import random
import time

import boto3
from apscheduler.schedulers.background import BackgroundScheduler
from dateutil.tz import tzutc

import config
import csvWriter
import jsonHandler
import s3Operations
import slackAlert

START = config.START
END = config.END
INCREMENT = config.INCREMENT
logging.basicConfig(filename='app.log', filemode='w', format='%(name)s - %(levelname)s - %(message)s')


def get_all_s3_keys():
    """
    Get a list of all keys in an S3 bucket.

    :param s3_path: Path of S3 dir.
    """
    endTime = datetime.datetime.now() + datetime.timedelta(hours=5, minutes=30, seconds=0, milliseconds=0)
    endDay = endTime.day
    endMonth = endTime.month
    endYear = endTime.year
    endHour = endTime.hour
    endMinute = endTime.minute
    endSecond = endTime.second
    startTime = endTime - datetime.timedelta(minutes=config.TIME_INTERVAL_IN_MINUTES, seconds=0, milliseconds=0)
    startDay = startTime.day
    startMonth = startTime.month
    startYear = startTime.year
    startHour = startTime.hour
    startMinute = startTime.minute
    startSecond = startTime.second
    print(startTime)
    print(endTime)
    s3 = boto3.client('s3')
    keys = []
    kwargs = {'Bucket': config.SNURL_BUCKET, 'Prefix': config.SNURL_BUCKET_PREFIX}
    resp=None
    while True:
        try:
            resp = s3.list_objects_v2(**kwargs)
            for obj in resp['Contents']:
                date = obj['LastModified'] + datetime.timedelta(hours=5, minutes=30, seconds=0, milliseconds=0)
                if (date >= datetime.datetime(startYear, startMonth, startDay, startHour, startMinute, startSecond,
                                              tzinfo=tzutc()) and date <= datetime.datetime(endYear, endMonth, endDay,
                                                                                            endHour, endMinute, endSecond,
                                                                                            tzinfo=tzutc())):
                    keyName = obj['Key']
                    print(keyName)
                    keys.append(keyName)
        except Exception as e:
            logging.exception("Exception occurred while listing keys",e)
        try:
            kwargs['ContinuationToken'] = resp['NextContinuationToken']
        except KeyError:
            break
    return keys


def makePayloads():
    listOfKeys = get_all_s3_keys()
    size = len(listOfKeys)
    det = []
    det.append("STARTING PAYLOAD CREATOR FOR TOTAL FILES : " + str(size) + " ON : " + str(
        datetime.datetime.now() + datetime.timedelta(hours=5, minutes=30, seconds=0, milliseconds=0)))
    csvWriter.write(config.CSVFILE_PATH, det)
    for keyName in listOfKeys:
        try:
            detailsToLog = []
            print(keyName)
            requestId = random.randrange(START, END, INCREMENT)
            filePath = config.INPUT_BUCKET_PREFIX + str(requestId) + config.JSON_EXTENSION
            s3Operations.download_file(config.SNURL_BUCKET, keyName, filePath)
            details = jsonHandler.readJsonAndAddRequestId(filePath, requestId)
            detailsToLog.append(keyName)
            detailsToLog.append(filePath)
            detailsToLog.append(details[0])
            detailsToLog.append(details[0] - details[1])
            csvWriter.write(config.CSVFILE_PATH, detailsToLog)
            if (len(details) != 0):
                slackAlert.sendNotification(
                    "Payload successfully created for " + keyName + ' in input/' + filePath + ' with Total profile URLs: ' + str(
                        details[0]) + ' and Duplicates :' + str(details[0] - details[1]))
                logging.info("Success for " + keyName)
                os.remove(filePath)
            else:
                logging.info("Failed for " + keyName)
                os.remove(filePath)
        except Exception as e:
            logging.exception("File not found....payload not created", e)


sched = BackgroundScheduler(daemon=True)
sched.add_job(makePayloads, 'interval', minutes=config.TIME_INTERVAL_IN_MINUTES)
sched.start()
while True:
    time.sleep(0)