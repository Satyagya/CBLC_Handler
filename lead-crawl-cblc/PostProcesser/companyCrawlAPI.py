import requests
import config
import traceback
import sys

headers = {'Content-type': 'application/json'}

url = config.COMPANY_CRAWL_API




def hitCompanyCrawlAPI(jsonToCrawl):
    '''
    hits company crawler API
    '''
    try:
        apiResponse = requests.post(url, data = jsonToCrawl, headers=headers)
        print('the api response is: ',apiResponse.status_code)
        return apiResponse
    except Exception as e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        tb = traceback.extract_tb(e.__traceback__)
        return f"Error occurred while hitting company crawl api with error = {e} with traceback = {tb} and type = {exc_type}"

