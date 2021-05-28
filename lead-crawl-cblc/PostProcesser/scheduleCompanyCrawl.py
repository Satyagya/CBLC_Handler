import codecs
import json
import os

import traceback
import sys
import constants
import service
import delete
import companyCrawlAPI
import slackAlert
import logging

requestList = []


formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')


def setup_logger(name, log_file, level=logging.DEBUG):
    """To setup as many loggers as you want"""

    handler = logging.FileHandler(log_file)
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)

    return logger


companyLogger = setup_logger(
    constants.COMPANY_LOGGER, constants.COMPANY_LOGGER_LOGS)


def startCompanyCrawlForScheduledRequests():
    '''
    to start the company crawl for scheduled requests
    '''
    data = {}
    requestId = None
    try:
        with codecs.open(os.getcwd()+constants.SLASH+constants.COMPANY_CRAWL_STATUS, constants.READ_MODE, encoding='utf-8-sig') as f:
            f.seek(0)
            file = json.load(f)
            # print('file is:',file, len(file))
            if (len(file)) > 0:
                companyCrawlData = file
                # json.loads(file)
                jsonArrayData = companyCrawlData[constants.DATA]
                print("size:", len(jsonArrayData))
                for i in jsonArrayData:
                    if i[constants.STATUS] == constants.SCHEDULED_STATUS:
                        requestId = i[constants.REQUEST_ID]
                        companiesName = i[constants.COMPANY_NAMES]
                        data[constants.REQUEST_ID] = int(requestId)
                        data[constants.COMPANY_NAMES] = companiesName
                        companiesToCrawl = json.dumps(data)
                        if len(companiesName) > 0:
                            print(
                                'performing company crawl for requestId: ', requestId)
                            apiResponse = companyCrawlAPI.hitCompanyCrawlAPI(
                                companiesToCrawl)

                            print('api response for company crawl is: ',
                                  apiResponse)
                            if "Error occurred" in apiResponse:
                                slackAlert.sendNotification(apiResponse)

                            elif apiResponse.status_code == 200:
                                service.updateCompanyCrawlStatus(
                                    requestId, constants.CRAWLING_STATUS)
                                slackAlert.sendNotification(
                                    f"company crawling started for requestId {requestId} with {len(companiesName)} number of companies")

                            else:
                                slackAlert.sendNotification(
                                    f"Error occurred during company crawl initiation for requestId {requestId} with {len(companiesName)} number of companies")
                        else:
                            service.mergeCompanyAndProfileData(requestId)
                            slackAlert.sendNotification(
                                f"No Company to crawl for {requestId}. Proceeding with Company Processing")

            else:
                print('no company to crawl')

        f.close()
    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        companyLogger.error(
            f"Error occurred while Company Processing initiation of requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
        slackAlert.sendNotification(
            f"Error occurred while Company Processing initiation of requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
