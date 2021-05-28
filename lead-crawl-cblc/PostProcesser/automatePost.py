import requests
import json
import s3_utils
import slackAlert
import datetime
import logging
import constants
import config
import traceback
import sys

formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')


def setup_logger(name, log_file, level=logging.INFO):
    """To setup as many loggers as you want"""

    handler = logging.FileHandler(log_file)
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)

    return logger


automatePostLogger = setup_logger(
    constants.AUTOMATE_POST_LOGGER, constants.AUTOMATE_PROCESSING_LOGS)


def getRequestIdForProfileProcessing():

    response = s3_utils.ls_object(
        config.BUCKET_NAME, config.BATCH_PRESENT_FOLDER)
    requestId = None
    response = sorted(response)
    i = response[0]
    # print(i)
    if len(i) > 0:
        i = i.split('/')[-1]
        i = i.split('.')[0]
        requestId = i

    return requestId


def hitApi():
    requestId = None
    try:
        requestId = getRequestIdForProfileProcessing()
        if requestId != None:
            # print('hitting api for: ', requestId,"with filename", fileName)
            automatePostLogger.info(
                f" Initiating Profile processing for requestId:{requestId}")
            try:
                apiResponse = requests.post(
                    config.PROFILE_PROCESSING_API + str(requestId))
            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                tb = traceback.extract_tb(e.__traceback__)
                automatePostLogger.error(
                    f"Error occurred while hitting Profile Processing API for requestId:{requestId}, Error = {e} traceback = {tb} error type = {exc_type}")
                slackAlert.sendNotification(
                    f"Error occurred while hitting Profile Processing API for requestId:{requestId}, Error = {e} traceback = {tb} error type = {exc_type}")

            if(apiResponse.status_code == 200):
                print("API hit successful")
                slackAlert.sendNotification("Profile Post Procesing started for " + str(requestId) + ' at ' +
                                            str(datetime.datetime.now()))
                automatePostLogger.info(
                    f"Profile Processing started at {datetime.datetime.now()} for requestId: {requestId}")
            else:
                if requestId != None:
                    slackAlert.sendNotification("Could not start Profile Post Processing for " + str(requestId) + ' at ' +
                                                str(datetime.datetime.now()) + f" with api response {apiResponse.content} and status {apiResponse.status_code}")
                    automatePostLogger.error(
                        f" Profile Processing initiation failed for requestId: {requestId} with API response: {apiResponse.status_code}")

                print("Failed to hit API")

    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        automatePostLogger.error(
            f" Error occurred while Profile Processing initiation of requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
        slackAlert.sendNotification(
            f"Error occurred while hitting Post Processing API with error={e} traceback = {tb} error type = {exc_type}")
