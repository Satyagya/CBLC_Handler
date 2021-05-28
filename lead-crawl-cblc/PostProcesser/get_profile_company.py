import calendar
import time
import logging
import traceback
from datetime import datetime,timezone
import html
from scrapy.selector import Selector
import codecs
import re
import os
import json
import config_Xpath
import io
# from extract_data import ExtractData
from extract_data import ExtractData
import sys
import traceback

from flask import jsonify
from os import path
import constants
from latest_company_details import LatestCompanyDetails
import sys
import pandas as pd
from s3Operations import move_object
import s3_utils
from argparse import ArgumentParser

df = pd.read_csv(config_Xpath.CSV_PATH)
File_Name = None

formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')


def setup_logger(name, log_file, level=logging.DEBUG):
    """To setup as many loggers as you want"""

    handler = logging.FileHandler(log_file)
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)

    return logger


profileLogger = setup_logger(constants.PROFILE_LOGGER, constants.PROFILE_LOGGER_LOGS)


def check_company(filename):
    """

    """
    global File_Name, a
    status = True
    print(os.getcwd())
    comp_dir_path = os.path.join(os.getcwd(), 'company_page')
    filelist = os.listdir(comp_dir_path)
    number_files = len(filelist)
    print(number_files)
    File_Name = os.listdir(comp_dir_path)
    print(File_Name)
    print(len(File_Name))
    return status


def get_profile_details(filename):
    """
    extracts all the details from the json file
    :params: filename of json file
    """
    global File_Name, a
    global comp_folder_path, profile_url, status
    print("File_Name in get_profile_details is ",File_Name)
    # profile_html = io.open(filename, constants.READ_MODE+'U',encoding='UTF-8')
    try:
        profile_html = open(filename, 'rb')
        profile_text = profile_html.read()
        profile_url = Selector(text=profile_text).css('link').xpath('@href').get()
    

        data = ExtractData()
        data.set_extract_data(Selector(text=profile_text))

        profile = data.__dict__

        # profile['name'] = Selector(text=profile_text).xpath(
        #     config_Xpath.LEAD_NAME_XPATH).extract_first().encode('ascii', 'ignore').decode()

        x1 = Selector(text=profile_text).xpath(
            config_Xpath.LEAD_NAME_XPATH).extract_first()
        if x1!=None:
            x1 = x1.encode(encoding='UTF-8', errors='strict').decode()
            x = html.unescape(str(x1))
            profile['name'] = x.replace("'", "").replace('"', '')


        #code to get all company urls 20/01/20

        company_urls = Selector(text=profile_text).xpath(
            config_Xpath.PROFILE_DISP).extract()

        #code segment end here

        profile['email_id'] = []
        profile['contact_number'] = []
        profile_pic = Selector(text=profile_text).xpath(
            config_Xpath.PROFILE_DISP).extract_first()
        profile['profile_picture'] = profile_pic
        if not profile_pic or profile_pic is None:
            profile['profile_picture'] = ''

        profile['profile_url'] = profile_url
        if profile_url is None:
            profile['profile_url'] = ''

        profile['url_hashcode'] = ''
        location = Selector(text=profile_text).xpath(
            config_Xpath.LOCATION_XPATH).extract()
        if location:
            if len(location) > 0:
                # print("location new is :",location[0])
                loc = str(location[0])
                # print("type is ",type(loc))
                profile['country'] = loc
                profile['state'] = ''
                profile['city'] = ''
            else:
                profile['country'] = ''
                profile['state'] = ''
                profile['city'] = ''
        else:
            profile['country'] = ''
            profile['state'] = ''
            profile['city'] = ''

       
        work_details = wd = profile['work_details']
        curr_populated = 0
        curr_orga = []
        if len(work_details) > 0:
            for i in range(len(work_details)):
                #print(work_details[i])
                #print(type(work_details[i]))
                if 'end_year' in work_details[i]:
                    if work_details[i]['end_year'] is 'Present':
                        curr_orga.append(work_details[i]['company'])
                        curr_populated = 1
                        # break

            # print("previous curr_orga",curr_orga)

            #set first chronological experience in latest experience
            if len(curr_orga) == 0 :
                work_details[0]['end_year'] = 'Present'
                work_details[0]['is_latest'] = True
                curr_orga.append(work_details[0]['company'])

            #end


            unique = []
            [unique.append(item) for item in curr_orga if item not in unique]
            # print("recent curr_orga",unique)
            # print(curr_orga)
            # input("enter")
            profile['latest_organization'] = unique
        else:
            profile['latest_organization'] = []

        #add code to previous-organization
        work_details = wd = profile['work_details']
        pwd_list = []
        if len(work_details) > 0:
            for i in range(len(work_details)):
                if 'end_year' in work_details[i]:
                    if work_details[i]['end_year'] is not 'Present':
                        pwd_list.append(work_details[i]['company'])

            pwd_list = list(set(pwd_list))
            # print(pwd_list)
            profile['previous_organization'] = pwd_list
        else:
            profile['previous_organization'] = []

        if len(work_details) != 0:

            #CODE ADDED
            curr_desg_list = []
            for i in range(len(work_details)):
                if 'end_year' in work_details[i]:
                    if work_details[i]['end_year'] == 'Present':
                        if work_details[i]['designation'] != '':
                            curr_desg = work_details[i]['designation']
                            # print("type of the present designation is:", type(work_details[i]['designation']))
                            if type(work_details[i]['designation']) == str:
                                # print("it's string")
                                curr_desg_list.append(curr_desg)
                            if type(work_details[i]['designation']) == list:
                                # print("it's list")
                                for j in range(len(work_details[i]['designation'])):
                                    curr_desg_list.append(work_details[i]['designation'][j])
            # print(curr_desg_list)

            # print("previous curr_desg_list", curr_desg_list)
            uniquedesg = []
            [uniquedesg.append(item) for item in curr_desg_list if item not in uniquedesg]
            # print("recent curr_desg_list", uniquedesg)
            profile['latest_designation'] = uniquedesg


            # print("File name is for website:", File_Name)

            profile['seniority_level'] = []
            profile['function'] = []

            # a = {}

            # print("File name is for latest_industry_type:", File_Name)


        else:
            profile['latest_designation'] = []
            profile['latest_company_size'] = []
            profile['latest_industry_type'] = []
            profile['seniority_level'] = []
            profile['function'] = []
            profile['website'] = []

        ed = profile['education_details']
        if len(ed) != 0:
            if 'college' in ed[0]:
                profile['latest_college'] = ed[0]['college'].encode(
                    'ascii', 'ignore').decode()

                #.extract_first().encode(encoding='UTF-8', errors='strict').decode()


            if 'degree' in ed[0]:
                profile['latest_degree'] = ed[0]['degree'].encode(
                    'ascii', 'ignore').decode()
        else:
            profile['latest_college'] = ''
            profile['latest_degree'] = ''

        experience = profile['work_details']
        if experience:
            current_companies = get_latest_company_urls(experience)


        #new code to set industry-size in work details
        a = {}
        x = a.keys()
        work_details = profile['work_details']
        # print("length of work details is :", work_details)
        if len(work_details) > 0:
            for i in range(len(work_details)):
                # print("company is :", work_details[i]['company'])
                comp_name = work_details[i]['company']
                #start
                if comp_name in x:
                    work_details[i]['company_size'] = a[comp_name][0]
                    work_details[i]['industry_type'] = a[comp_name][1]
                #end

        #code to get company url of enddate as present



        #code to convert present in wd
        work_details = wd = profile['work_details']
        if len(work_details) > 0:
            for i in range(len(work_details)):
                # print(work_details[i])
                # print(type(work_details[i]))
                if 'end_year' in work_details[i]:
                    if work_details[i]['end_year'] is 'Present':
                        tsnew = datetime.now().timestamp()
                        ts1 = int(tsnew)
                        ts2 = str(ts1)
                        work_details[i]['end_year'] = ts2


                        # tsnew = datetime.datetime.now().timestamp()
                        # ts1 = int(tsnew)
                        # ts2 = str(ts1)
                        # work_details[i]['end_year'] = ts2


        #code to convert present in ed
        ed = profile['education_details']
        if len(ed) > 0:
            for i in range(len(ed)):
                # print(ed[i])
                # print(type(ed[i]))
                if 'end_year' in ed[i]:
                    if ed[i]['end_year'] is 'Present':
                        tsnew = datetime.now().timestamp()
                        ts1 = int(tsnew)
                        ts2 = str(ts1)
                        ed[i]['end_year'] = ts2

                
        curr_expe_months = []
        wd1 = profile['work_details']
        if len(wd1) > 0:
                for i in range(len(wd1)):
                    if 'end_year' in wd1[i]:
                        if wd1[i]['is_latest'] is True:
                            if (len(wd1[i]['start_year']) != 0) and (len(wd1[i]['end_year']) != 0):
                                tsnew1 = wd1[i]['start_year']
                                tsnew2 = wd1[i]['end_year']
                                ts11 = int(tsnew1)
                                ts21 = int(tsnew2)
                                tsdiff = ts21 - ts11
                                tsdiff_months = int(tsdiff / (60 * 60 * 24 * 30))
                                curr_expe_months.append(tsdiff_months)
        profile['current_experience_in_months'] = curr_expe_months


        total_expe_months = 0
        wd2 = profile['work_details']
        if len(wd2) > 0:
                for i in range(len(wd2)):
                    if 'end_year' in wd2[i]:
                        if (len(wd2[i]['start_year']) != 0) and (len(wd2[i]['end_year']) != 0):
                            tsnew11 = wd2[i]['start_year']
                            tsnew21 = wd2[i]['end_year']
                            ts111 = int(tsnew11)
                            ts211 = int(tsnew21)
                            tsdiff1 = ts211 - ts111
                            tsdiff1_months = int(tsdiff1/(60*60*24*30))
                            total_expe_months = total_expe_months + tsdiff1_months
        profile['total_experience_in_months'] = total_expe_months




        #add to set ed-details[0] to true
        ed = profile['education_details']
        if len(ed) > 0:
            if 'is_latest' in ed[0]:
                ed[0]['is_latest'] = True


        return profile
    except Exception as e:
        print("final modified value is in EXCEPTION block :", profile)
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        profileLogger.error(f"Error occurred during json processing of filename: {filename} with error: {e} traceback: {tb}" +
                            f" {exc_type}")
        return False


def get_latest_company_urls(experience):
    if experience:
        return [item['company_url'] for item in experience if 'Present' in item['end_year'] and 'company_url' in item.keys()]


def get_all_company_urls(experience):
    if experience:
        return [item['company_url'] for item in experience if 'company_url' in item.keys()]


def get_total_work_experience(experience):
    years = []
    months = []
    total_experience = ''
    for item in experience:
        if 'duration' in item.keys():
            months.append(int(item['duration']))
    if months:
        total_months = sum(months)
        t = total_months // 12
        if t * 12 == total_months:
            x = "{} years".format(t)
        else:
            x = "More than {} years".format(t)
        total_experience = x
    else:
        total_experience = ''
    return total_experience



def postProcessing(requestId):
    # companyStatus = check_company()
    fileDirectory = str(requestId)
    if path.exists(constants.PROFILE_HTML_DIRECTORY + constants.SLASH + fileDirectory):
        print("cheking directory: " + constants.PROFILE_HTML_DIRECTORY +
              constants.SLASH + fileDirectory)
        files = os.listdir(constants.PROFILE_HTML_DIRECTORY +
                           constants.SLASH + fileDirectory)
        for file in files:
            json_file = get_profile_details(
                constants.PROFILE_HTML_DIRECTORY + constants.SLASH + fileDirectory + constants.SLASH + file)
            if json_file == False:
                profileLogger.error(f"Error occurred in json making for file: {file} in requestId: {requestId}")
                continue
            else:
                file = str(file).split(constants.DOT)
                filename = file[0]
                filename = re.sub('[^a-zA-Z0-9 ]', '', filename)
                if constants.SPACE in filename:
                    filename = filename.replace(constants.SPACE, constants.BLANK)
                try:
                    s4 = constants.LOCAL_SOURCE_LINKEDIN + str(requestId)+constants.UNDERSCORE +\
                        filename + constants.JSON_EXTENSION
                    with open(os.path.join('profile_json', s4), 'w', encoding='utf-8') as fp:
                        json.dump(json_file, fp, ensure_ascii=False)                    
                except Exception as e:
                    exc_type, exc_value, exc_traceback = sys.exc_info()
                    tb = traceback.extract_tb(e.__traceback__)
                    profileLogger.error(
                        f" Error occurred while json processing for requestId: {requestId} for {filename}, Error = {e} traceback = {tb}  error type = {exc_type}")

        return True
    else:
        profileLogger.error(
            f" Error occurred while moving zip file for requestId: {requestId}, Error = {e} traceback = {tb}  error type = {exc_type}")

        return False


# postProcessing(1011686)
