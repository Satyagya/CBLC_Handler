import os
import io
import time
from os import *
from flask import Flask, request, flash, redirect, url_for, jsonify
from werkzeug.utils import secure_filename
from zipfile import ZipFile
import shutil
import re
from difflib import SequenceMatcher
from fuzzywuzzy import fuzz
import math
from glob import glob
from time import *
import json
import sys
import traceback
import datetime
import csvWriter
import codecs
import logging

import s3Operations
import config
import config_Xpath
import get_profile_company
import s3_utils
import fileOperations
import payload
import jsonHandler
import delete
import constants
import unzip
from threading import Thread
import re
import downloadFiles
import companyCrawlAPI
import slackAlert

headers = {'Content-type': 'application/json'}
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')


def setup_logger(name, log_file, level=logging.DEBUG):
    """To setup as many loggers as you want"""

    handler = logging.FileHandler(log_file)
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)

    return logger

companyLogger = setup_logger(constants.COMPANY_LOGGER, constants.COMPANY_LOGGER_LOGS)
profileLogger = setup_logger(constants.PROFILE_LOGGER, constants.PROFILE_LOGGER_LOGS)
s3Logger = setup_logger(constants.S3LOGGER, constants.S3_PROCESSING_LOGS)


class CompanyPostProcessing(Thread):
    def __init__(self, requestId):
        Thread.__init__(self)
        self.requestId = requestId
    
    def run(self):
        
        print('starting company processing: ')
        requestId = self.requestId
        companyLogger.info(
            f"Initiating Company processing for requestId: {requestId}")
        createCompanyJsonDirectory(requestId)
        companyLogger.info(
            f"Company Json directory completed for requestId: {requestId}")
        try:
            details = []
            details.append(datetime.datetime.now())
            details.append(requestId)

            downloadFiles.fetchProfileJson(requestId)
            companyLogger.info(
                f" All profilejson fetched for {requestId}")
            
            updateCompanyCrawlStatus(requestId, constants.COMPLETE_STATUS)
            

            listOfProfileJson = glob(constants.MERGING_JSON_DATA + constants.SLASH +
                                    constants.LOCAL_SOURCE_LINKEDIN+str(requestId)+constants.UNDERSCORE + constants.ALL_JSON_FILES)

            companyLogger.info(f" Number of profile json fetched: {len(listOfProfileJson)}")
            for profileJson in listOfProfileJson:
                jsonData = None
                websites = []
                company_size = []
                industry_type = []
                temp = []

                companiesInLatestOrganization = []
                try:
                    with codecs.open(profileJson, 'r+', 'UTF-8') as f:
                        jsonData = json.load(f)
                        # print('json data is: ', jsonData)
                        companiesInLatestOrganization = jsonData[constants.LATEST_ORGANIZATION]

                        if len(companiesInLatestOrganization) > 0 and companiesInLatestOrganization[0]!='':
                            for eachCompany in companiesInLatestOrganization:

                                if '/' in eachCompany:
                                    companyNameList = eachCompany.split('/')
                                    for i in companyNameList:
                                        i = i.replace(" ", "")
                                        print("companyName to check inside main with list: ", i)
                                        websites, company_size, industry_type = addDetailsInRequiredList(websites,
                                        company_size, industry_type, i, requestId)
                                else:
                                    companyName = eachCompany



                                    websites, company_size, industry_type = addDetailsInRequiredList(websites,
                                    company_size, industry_type, companyName, requestId)

                            jsonData[constants.LATEST_INDUSTRY_TYPE] = industry_type
                            jsonData[constants.LATEST_COMPANY_SIZE] = company_size
                            jsonData[constants.WEBSITE] = websites

                        else:
                            jsonData[constants.LATEST_INDUSTRY_TYPE] = industry_type
                            jsonData[constants.LATEST_COMPANY_SIZE] = company_size
                            jsonData[constants.WEBSITE] = websites

                        f.seek(0)
                        f.truncate()
                        json.dump(jsonData, f)
                        f.close()
                
                except Exception as e:
                    exc_type, exc_value, exc_traceback = sys.exc_info()
                    tb = traceback.extract_tb(e.__traceback__)
                    companyLogger.error(
                        f"Error occurred while Company Processing initiation of requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")

            companyLogger.info(f" Company Details added for All Profiles for requestId: {requestId}")
            
            
            if finalJsonProcessing(requestId):
                details.append(constants.COMPLETE_STATUS)
                csvWriter.write(config.OUTPUT_LOGD_CSV, details)

                slackAlert.sendNotification("Company Post Processing completed for requestID " + str(requestId) + ' at ' +
                                            str(datetime.datetime.now()) + ' and Final json have been uploaded')
                

            else:
                details.append('UNSUCCESSFULL IN FINAL JSON PROCESSING')
                companyLogger.error(
                    f" Error in Final Json Processing for requestId: {requestId}")
                csvWriter.write(config.OUTPUT_LOGD_CSV, details)
                slackAlert.sendNotification("Error Occurred during Final Json Processing for requestID " + str(requestId) + ' at ' +
                                            str(datetime.datetime.now()))
              

        except Exception as e:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            tb = traceback.extract_tb(e.__traceback__)
            details.append('EXCEPTION OCCURRED')
            companyLogger.error(
                f" Error in Company Processing requestId: {requestId}, Error = {e}")
            companyLogger.error(
                f" stack trace for requestId: {requestId}, Error = {e}, traceback = {tb}")
            csvWriter.write(config.OUTPUT_LOGD_CSV, details)
            slackAlert.sendNotification("Error Occurred during Company Post Processing for requestID " + str(requestId) + ' at ' +
                                        str(datetime.datetime.now()) + f"with error = {e} with stack trace = {tb} error type = {exc_type}")

        finally:
            companyLogger.info(
                f"deleting all processed files for requestId {requestId}")
            deleteFilesAfterPostProcessing(requestId)


class ProfilePostProcessing(Thread):
    def __init__(self, requestId):
        Thread.__init__(self)
        self.requestId = requestId
        # self.filename = filename
    def run(self):

        print("Starting profile processing: ")
        requestId = self.requestId
        # filename = str(requestId) + constants.ZIP_EXTENSION
        profileLogger.info(
            f" Initiating Profile Processing for requestId: {requestId}")

        postProcessingFlag = False
        try:

            unzip.extract_zip_file(requestId)
            sleep(1)
            get_profile_company.postProcessing(requestId)
            profileLogger.info(
                f" Profile HTML Processing completed for requestId: {requestId}")
            print('***------------***')
            sleep(1)
            s3_utils.upload_profile_html_s3(requestId)
            profileLogger.info(
                f" Profile HTML uploaded for requestId: {requestId}")
            sleep(1)
            s3_utils.upload_profile_json(requestId)
            profileLogger.info(
                f" Profile JSON uploaded for requestId: {requestId}")
            sleep(1)
            addRequestForCompanyCrawl(requestId)
            
            moveZipFileAfterProcessing(requestId)
            profileLogger.info(
                f"Profile Post Processing completed for requestId: {requestId}")
            slackAlert.sendNotification(f"Profile Post Processing completed for requestId: {requestId} at {datetime.datetime.now()}")

        except Exception as e:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            tb = traceback.extract_tb(e.__traceback__)
            profileLogger.error(
                f" Error occurred during Profile Processing for requestId: {requestId}, Error = {e}")
            profileLogger.error(
                f" Error occurred = {tb}")
            slackAlert.sendNotification("Error Occurred during Profile Post Processing for requestID " + str(requestId) + ' at ' +
                    str(datetime.datetime.now()) + f"with error = {e} with traceback = {tb} with error = {exc_type}")
            message = str(e)
            resp = jsonify({'message': message})
            resp.status_code = 500
            return resp

        finally:
            deleteAllFilesBeforeCompanyProcessing(requestId)


def createCompanyJsonDirectory(requestId):
    pathToCheck = constants.COMPANY_JSON_DIRECTORY + constants.SLASH + \
        constants.COMPANY + constants.UNDERSCORE + str(requestId)
    if path.exists(pathToCheck) == False:
        mkdir(pathToCheck)


def checkFalsePositive(crawledOne, latestOrganisation):
    # crawledOne = "180 Degree Consulting"
    # latestOrganisation = "180DegreeConsultingPvtLtd"
    s1 = re.sub('[^A-Za-z0-9]+', '', crawledOne.lower())
    s2 = re.sub('[^A-Za-z0-9]+', '', latestOrganisation.lower())
    s = SequenceMatcher(None, s1, s2)
    sequenceRatio = math.ceil(s.ratio()*100)
    fuzzRatio = fuzz.ratio(s1, s2)
    if s1 is s2 or s1 in s2 or s2 in s1 or (sequenceRatio > 90 and fuzzRatio > 90):
        # print(crawledOne, latestOrganisation, True)
        return True
    else:
        # print(crawledOne, latestOrganisation, False)
        return False


def moveZipFileAfterProcessing(requestId):
    # fileName = fileName.split(constants.UNDERSCORE)[-1]
    # print(fileName)
    filepath=None
    filepath = downloadFiles.findZipWithRequestId(requestId)
    locationToMove = config.POST_PROCESSING_COMPLETED_ZIP +\
        constants.SLASH + str(requestId)+constants.ZIP_EXTENSION
    try:
        if filepath!=None:
            s3Operations.move_object(config.BUCKET_NAME, filepath, config.BUCKET_NAME, locationToMove)
            profileLogger.info(
                f" Zip files moved to {config.POST_PROCESSING_COMPLETED_ZIP} after processing for requestId: {requestId}")
            print('file moved after processing')
        else:
            profileLogger.error(
                f" Error in moving zip file to {config.POST_PROCESSING_COMPLETED_ZIP} after profile processing for requestId: {requestId}")
            print('error in moving files to processed zip')
    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        s3Logger.error(
            f" Error occurred while moving zip file for requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
        slackAlert.sendNotification(
            f"Error occurred while moving zip files for requestId:: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")


def addDetailsInRequiredList(websiteList, companySizeList, industryTypeList, companyName, requestId):

    if checkCompanyJsonExist(companyName, requestId): # True if companyjson does not exist
        num = -99
        num = downloadFiles.fetchCompanyJson(companyName, requestId)
        print("company data fetched")
        if num == 1:
            companyData = loadCompanyJson(companyName, requestId)
            # print('type of companyData', type(companyData))
            if str(type(companyData)) != constants.NONETYPE:
                if companyData != None:
                    if len(companyData) > 0:
                        industryTypeList.append(companyData[constants.INDUSTRY])
                        # jsonData['latest_industry_type'] = industryTypeList
                        companySizeList.append(
                            companyData[constants.EMPLOYEE_COUNT])
                        # jsonData['latest_company_size'] = companySizeList
                        websiteList.append(companyData[constants.WEBSITE_URL])


    else:
        companyData = loadCompanyJson(companyName, requestId)
        if str(type(companyData)) != constants.NONETYPE:
            if companyData != None:
                if len(companyData) > 0:
                    industryTypeList.append(
                        companyData[constants.INDUSTRY])
                    # jsonData['latest_industry_type'] = industryTypeList
                    companySizeList.append(
                        companyData[constants.EMPLOYEE_COUNT])
                    # jsonData['latest_company_size'] = companySizeList
                    websiteList.append(companyData[constants.WEBSITE_URL])

    return websiteList, companySizeList, industryTypeList


def addRequestForCompanyCrawl(requestId):
    '''
    adds the request for company crawl
    :param: requestId
    '''
    data={}
    requestId = int(requestId)
    data[constants.REQUEST_ID] = requestId
    data[constants.STATUS] = constants.SCHEDULED_STATUS
    companyList = generateCompanyNameToBeCrawled(requestId)
    data[constants.COMPANY_NAMES] = companyList
    jsonArrayData=[]
    flag=True
    try:
        if os.path.isfile(os.getcwd()+constants.SLASH+constants.COMPANY_CRAWL_STATUS):
            with codecs.open(os.getcwd()+constants.SLASH+constants.COMPANY_CRAWL_STATUS, 'r+', encoding='utf-8-sig') as f:
                # file = f.read()
                f.seek(0)
                file = json.load(f)
                companyCrawlData={}

                if(len(file))>0:
                    companyCrawlData = file
                    jsonArrayData = companyCrawlData[constants.DATA] #json.loads(file)
                    print('take 1: ',len(jsonArrayData))

                    for i in jsonArrayData:
                        # print(i)
                        if i[constants.REQUEST_ID] == requestId:
                            print('request already exist')
                            flag=False
                    if flag==True:
                        jsonArrayData.append(data)
                        companyCrawlData[constants.DATA] = jsonArrayData
                        # print('take 2: ', jsonArrayData)
                        f.seek(0)  # sets  point at the beginning of the file
                        f.truncate()
                        json.dump(companyCrawlData, f)
                else:
                    jsonArrayData.append(data)
                    companyCrawlData[constants.DATA] = jsonArrayData
                    json.dump(companyCrawlData, f)

                f.close()

            profileLogger.info(
                f" {requestId} added for Company Crawl")
        else:
            profileLogger.error(
                f" CompanyCrawlStatus.json could not be found")
            print('no json found for company crawling status')
    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        s3Logger.error(
            f"Error occurred while adding companies for company crawl for requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
        slackAlert.sendNotification(
            f"Error occurred while adding companies for company crawl for requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")


def generateCompanyNameToBeCrawled(requestId):
    '''
    generate company name json to be crawled
    '''
    
    companyList=[]
    listOfProfileJson = glob(os.getcwd()+constants.SLASH+constants.PROFILE_JSON_DIRECTORY+\
        constants.LOCAL_SOURCE_LINKEDIN+str(requestId) + constants.ALL_JSON_FILES )
    print(len(listOfProfileJson))
    for profileJson in listOfProfileJson:
        jsonData=None
        with codecs.open(profileJson, constants.READ_MODE, 'UTF-8') as f:
            jsonData = json.load(f)
        # print(jsonData)
        workDetails = jsonData[constants.WORK_DETAILS]
        # print(len(workDetails))
        for eachDetail in workDetails:
            response = None
            # print('each_details is: ', eachDetail)
            if eachDetail[constants.IS_LATEST] == True:
                companyName = eachDetail[constants.COMPANY]
                if str(type(companyName)) != constants.NONETYPE and companyName != None and companyName != 'None':
                    # print("company name:", companyName)
                    # companyName = companyName.replace(" ", "")
                
                    if '/' in companyName:
                        tempCompanyList = companyName.split(constants.SLASH)
                        # print('companyList is :', tempCompanyList)
                        for i in tempCompanyList:
                            if i not in constants.REMOVE_COMPANY_LIST:    
                                response = downloadFiles.findCompanyWithName(i)
                                print('response for companyName: ',i ,' is ',response)
                                if response == None:
                                    companyList.append(str(i))
                                    # print('companyList is: ',companyList)

                    else:
                        
                        response = downloadFiles.findCompanyWithName(companyName)

                        if response == None:
                            companyList.append(str(companyName))
                        else:
                            continue
            else:
                continue
    profileLogger.info(
        f" CompanyList to crawl generated for requestId: {requestId}")
    companyList = list(set(companyList))
    return companyList
    
    


def finalJsonProcessing(requestId):
    '''
    for producing final json after post processing
    params:
    deviceId: provided by mobile app
    requestId: provided by mobile app
    '''
    sleep(1)
    
    json_list = []
    fname = []
    final_data = dict()
    jsonDataList = glob(constants.MERGING_JSON_DATA + constants.SLASH +
                        constants.LOCAL_SOURCE_LINKEDIN+str(requestId)+constants.UNDERSCORE + constants.ALL_JSON_FILES)
    for i in jsonDataList:
        if re.search(str(requestId), i):
            fname.append(i)
    print("******************")
   
    if len(fname) > 0:
        details = []
        details.append(datetime.datetime.now())
        for json_path in fname:
            
            if re.search(str(requestId), json_path):
                pathToCheck = os.getcwd()+constants.SLASH+json_path
                try:
                    with codecs.open(pathToCheck, constants.READ_MODE, 'UTF-8') as f:
                        data = json.load(f)
                        # print(data)
                        json_list.append(data)
                except Exception as e:
                    print('the error we have: ' + e)
                    details.append(json_path)
                    details.append(e)
                    csvWriter.write(config.ERROR_FINAL_JSON, details)
                    continue
        #print("json list is", json_list)
        final_data[constants.ENTITIES] = json_list
        final_data[constants.REQUEST_ID] = int(requestId)
        s4 = config_Xpath.pref + str(requestId) + constants.JSON_EXTENSION
        try:
            with codecs.open(os.path.join(constants.FINAL_JSON_DIRECTORY, s4), constants.WRITE_MODE) as fp:
                # with open('profile_json/result2.json', 'w') as fp:
                json.dump(final_data, fp)
        except Exception as e:
            exc_type, exc_value, exc_traceback = sys.exc_info()
            tb = traceback.extract_tb(e.__traceback__)
            companyLogger.error(
                f" Error occurred while creating final json for requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")

    companyLogger.info(
        f" Final Json Processed for requestId: {requestId}")
    sleep(1)
    return s3_utils.uploadFinalJson()
        

def loadCompanyJson(companyName, requestId):
    companyNameWithoutSpace=None
    if " " in companyName:
        companyNameWithoutSpace = companyName.replace(" ","")
    else:
        companyNameWithoutSpace = companyName
    removeList = constants.REMOVE_COMPANY_LIST
    pathToCheck = os.getcwd()+constants.SLASH+constants.COMPANY_JSON_DIRECTORY + constants.SLASH +\
        constants.COMPANY+constants.UNDERSCORE+str(requestId)
    listOfFiles = os.listdir(pathToCheck)
    for i in listOfFiles:
       
        name = str(i).split(constants.DOT)[0]
        # print(name, companyNameWithoutSpace)
        if companyNameWithoutSpace == name and companyNameWithoutSpace != constants.DS_STORE and companyNameWithoutSpace not in removeList:
            print("inside -> loading for: ", companyNameWithoutSpace)

            try:
                with codecs.open(pathToCheck + constants.SLASH + i, 'r', 'utf-8') as f:
                    # file = f.read()
                    data = json.load(f)
                    res = not bool(data)
                    if  not res:
                        # print(res, data)
                        if checkFalsePositive(data[constants.NAME], companyName):
                            return data
                        else:
                            return {}

            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                tb = traceback.extract_tb(e.__traceback__)
                companyLogger.error(f"Error occurred while loading company {i} json with error = {e}")
                slackAlert.sendNotification(
                    f"Error occurred while loading company {i} with error = {e} and traceback = {tb}  {exc_type} while company post processing")
                return {}

def checkCompanyJsonExist(companyName, requestId):
    '''
    check whether company Json exists or not
    '''
    if " " in companyName:
        companyName = companyName.replace(" ", "")
    pathToCheck = os.getcwd()+constants.SLASH+constants.COMPANY_JSON_DIRECTORY+constants.SLASH + \
        constants.COMPANY+constants.UNDERSCORE+str(requestId)
    listOfFiles = os.listdir(pathToCheck)
    # print("files inside:", pathToCheck, len(listOfFiles))
    for i in listOfFiles:
        if companyName in str(i):
            return False

    return True


def updateCompanyCrawlStatus(requestId, status):
    '''
    update the status in the company crawl status json file wrt to requestId
    '''
    file = None
    try:
        with codecs.open(os.getcwd()+constants.SLASH+constants.COMPANY_CRAWL_STATUS, 'r+', encoding='utf-8-sig') as f:
            f.seek(0)
            file = json.load(f)
            print(len(file["DATA"]))
            if(len(file))>0:
                companyCrawlData = file
                jsonArrayData = companyCrawlData[constants.DATA]
                for i in jsonArrayData:
                    if i["REQUEST_ID"] == requestId:
                        i[constants.STATUS] = status
                        f.seek(0)
                        f.truncate()
                        companyCrawlData[constants.DATA] = jsonArrayData
                        print(i[constants.STATUS], i[constants.REQUEST_ID])
                        json.dump(companyCrawlData, f)
                        f.close()
    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        companyLogger.error(
            f" Error occurred while updating Company Crawl Status, Company Processing of requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")
        slackAlert.sendNotification(
            f"Error occurred while updating Company Crawl Status, Company Processing of requestId: {requestId} with error={e} traceback = {tb} error type = {exc_type}")


def mergeCompanyAndProfileData(requestId):
    if requestId==None or len(str(requestId))<=0:
        resp = jsonify({'message': "wrong requestId provided"})
        resp.status_code = 400
        return resp
    else:
        try:
            postProcessingThread = CompanyPostProcessing(requestId)
            postProcessingThread.start()
            flag = True
        except Exception as e:
            flag==False
            print(e)



    if flag == True:
        resp = jsonify({'message': constants.SUCCESS})
        resp.status_code = 200
        return resp
    else:
        resp = jsonify({'message': "some error occcurred"})
        resp.status_code = 500
        return resp


                    


def allowed_file(filename):
    '''
    this allows only zip file to be uploaded by the device

    '''
    extension = str(filename).split('.') #add
    if extension[-1] in constants.ALLOWED_EXTENSIONS:
        return True
    else:
        return False

def makeDirectories():
    '''
    this makes the required directories to be used
    '''
    if path.isdir(path.join(getcwd(), constants.FINAL_JSON_DIRECTORY)) == False:
        os.mkdir(path.join(getcwd(), constants.FINAL_JSON_DIRECTORY))
    if path.isdir(path.join(getcwd(), constants.COMPANY_JSON_DIRECTORY)) == False:
        os.mkdir(path.join(getcwd(), constants.COMPANY_JSON_DIRECTORY))
    if path.isdir(path.join(getcwd(), constants.PROFILE_HTML_DIRECTORY)) == False:
        os.mkdir(path.join(getcwd(), constants.PROFILE_HTML_DIRECTORY))
    if path.isdir(path.join(getcwd(), constants.PROFILE_JSON_DIRECTORY)) == False:
        os.mkdir(path.join(getcwd(), constants.PROFILE_JSON_DIRECTORY))
    if path.isdir(path.join(getcwd(), constants.ASSETS_DIRECTORY)) == False:
        os.mkdir(path.join(getcwd(), constants.ASSETS_DIRECTORY))
    if path.isdir(path.join(getcwd(), constants.MERGING_JSON_DATA)) == False:
        os.mkdir(path.join(getcwd(), constants.MERGING_JSON_DATA))
    

def deleteFilesAfterPostProcessing(requestId):
    '''
    this deletes all files generated during the whole process
    :param: requestId
    '''
    try:
        delete.deleteCompanyJsonForRequestId(requestId)
        delete.deleteAllMergedJson(requestId)     
        # delete.deleteFinalJson(requestId)
        companyLogger.info(
            f" Merge JSONs and Final JSON deleted for{requestId}")
    except Exception as e:
        companyLogger.error(
            f" Error in deleting Merge JSONs and Final JSON for{requestId}, Error = {e}")
        print(f"error occurred: {e}")

def deleteAllFilesBeforeCompanyProcessing(requestId):
    '''
    this deletes all files generated during the whole process
    :param: requestId
    '''
    try:
        delete.deleteZip(requestId)
        delete.deleteAllProfileJson(requestId)
        delete.deleteAllProfileHTML(requestId)
        profileLogger.info(
            f" Profile JSONs, zip and profile htmls deleted for {requestId}")
    except Exception as e:
        profileLogger.error(
            f" Error in deleting Profile JSONs, zip and profile htmls for{requestId}, Error = {e}")
        print(f"error occurred: {e}")

        

def processAndUploadProfile(requestId):
    '''
    downloads the profile zip and starts the post processing of the files
    :param: requestId
    :param: fileName
    returns success or failure response
    '''
    filename = downloadFiles.fetchProfileZip(requestId)
    print('file is:',requestId)
    flag = False
    details = []
    details.append(datetime.datetime.now())
    details.append(requestId)

    if requestId == None:
        resp = jsonify({'message': "no requestId provided"})
        resp.status_code = 400
        return resp

    if filename == constants.NO_FILE_FOUND:
        resp = jsonify({'message': constants.UNABLE_TO_FIND})
        resp.status_code = 400
        return resp
    else:
        try:
            postProcessingThread = ProfilePostProcessing(requestId)
            postProcessingThread.start()
            flag = True
        except Exception as e:
            flag==False
            print(e, e.with_traceback)



    if flag == True:
        resp = jsonify({'message': constants.SUCCESS})
        resp.status_code = 200
        return resp
    else:
        resp = jsonify({'message': constants.UPLOAD_ERROR})
        resp.status_code = 400
        return resp

