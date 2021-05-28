import json
import os
import random
import re

import config


def getData():
    """
    gets the json data
    :return:
    """
    with open(config.LOCAL_FILE_PATH) as f:
        data = json.load(f)
    return data

def readJsonAndAddRequestId(filePath):
    if (os.path.exists(filePath)):
        with open(filePath) as f:
            data = json.load(f)
        profileUrlList = data[config.PROFILE_URL]
        requestId = data[config.REQUEST_ID]
        return requestId
    return None
