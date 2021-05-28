import datetime
import logging
import os
import slackAlert
from flask import jsonify

import config
import csvWriter
import getFile
import jsonHandler
import s3Operations


def fetchPayload(deviceId):
    """
    fetches payload and returns json data as result
    :param deviceId:
    :return: json data
    """
    filePath = getFile.fileWithLeastTimestamp()
    if (filePath):
        try:
            details = []
            requestId = None
            s3Operations.download_file(
                config.BUCKET_NAME, filePath, config.LOCAL_FILE_PATH)
            if (os.path.exists(config.LOCAL_FILE_PATH)):
                requestId = jsonHandler.readJsonAndAddRequestId(
                    config.LOCAL_FILE_PATH)
                jsonData = jsonHandler.getData()
            details.append(deviceId)
            details.append(requestId)
            details.append(filePath)
            details.append(datetime.datetime.now())
            csvWriter.write(config.INPUT_LOGS_CSV, details)
            s3Operations.move_object(
                config.BUCKET_NAME, filePath, config.BUCKET_NAME, config.PROCESSED_BUCKET + str(requestId))
            slackAlert.sendNotification("Payload provided to the device with DeviceId: "+str(
                deviceId)+" RequestId: "+str(requestId)+" File path : "+str(filePath)+" on "+str(datetime.datetime.now()))
            return jsonData
        except Exception as e:
            logging.error(e)
            print(
                f'Unexpected error while downloading from {config.BUCKET_NAME}:{filePath}')
            slackAlert.sendNotification("Unexpected error occurred at server while fetching payload for "+str(
                deviceId)+" RequestId: "+str(requestId)+" on "+str(datetime.datetime.now()))
            raise
    else:
        print("No recent files to crawl found!!")
        resp = jsonify(
            {"PROFILE_URL": "Currently payloads are not available !!", "REQUEST_ID": 0})
        resp.status_code = 404
        slackAlert.sendNotification(
            "Currently payloads are not available !! requested by DeviceId: " + str(deviceId) + " on " + str(datetime.datetime.now()))
        return resp
