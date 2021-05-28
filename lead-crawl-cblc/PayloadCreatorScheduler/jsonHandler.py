import json
import os
import re
import config
import S3Lookup
import s3Operations

def getNameFromProfile(url):
    """extracts name from the profile"""
    regex = config.EXTRACT_NAME_REGEX
    name = re.findall(regex, url)
    if len(name) != 0:
        return url.replace(name[0], config.BLANK)

def checkUrl(url):
    """checks for valid url"""
    newUrl = None
    if re.search(config.LINKEDIN_URL_REGEX, url):
        newUrl = url
        if (re.search(config.LINKEDIN_QUESTION_FIND_REGEX, url)):
            newUrl = re.sub(config.LINKEDIN_SUB_REGEX, config.BLANK, url)
    return newUrl


def checkDuplicate(url):
    """checks for duplicate url"""
    profileName = getNameFromProfile(url)
    flag = S3Lookup.checkDuplicate(profileName)
    if flag:
        return False
    return True


def readJsonAndAddRequestId(filePath, requestId):
    """
    reads json and adds random request Id
    :param filePath: path of the file downloaded from s3
    :param requestId: random request generated
    :return:
    """
    details = []
    if (os.path.exists(filePath)):
        with open(filePath) as f:
            data = json.load(f)
        details.append(len(data[config.PROFILE_URL]))
        profileUrlList = data[config.PROFILE_URL]
        newProfileUrlList = []
        for url in profileUrlList:
            resp = None
            if (url):
                resp = checkUrl(url)
            if (resp):
                flag = checkDuplicate(resp)
                if (flag):
                    newProfileUrlList.append(resp)
        data[config.PROFILE_URL] = newProfileUrlList
        data[config.REQUEST_ID] = requestId
        details.append(len(newProfileUrlList))
        with open(filePath, 'w') as f:
            json.dump(data, f)
        s3Operations.upload_file(config.BUCKET_NAME, filePath, config.INPUT_BUCKET_PATH + filePath)
        return details
    return None
