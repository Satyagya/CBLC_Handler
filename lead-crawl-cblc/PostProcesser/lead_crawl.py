import threading
from apscheduler.schedulers.background import BackgroundScheduler
from flask import Flask, request, jsonify

import constants
import fileOperations
import payload
import scheduleCompanyCrawl
import service
import automatePost
import config
import automatePost

app = Flask(__name__)
app.config[constants.UPLOAD_FOLDER_NAME] = constants.UPLOAD_FOLDER

lock = threading.Lock()


@app.route("/getPayload", methods=['GET'])
def getPayload():
    """
    for providing payload to the requested device
    :return: json data containing URLs and requestId
    """

    # << -- auth code -- >>
    # deviceId = request.args.get('deviceId')
    # if (None == deviceId):
    #     response = jsonify(
    #         {"PROFILE_URL": "please provide deviceId", "REQUEST_ID": 0})
    #     response.status_code = 403
    #     return response
    # keyForAuthCheck=config.AUTH_ACTIVE_PATH+deviceId
    # if(authCheck.auth_lookup(keyForAuthCheck)):
    #     lock.acquire()
    #     response = payload.fetchPayload(deviceId)
    #     lock.release()
    #     return response
    # else:
    #     response = jsonify({"PROFILE_URL": "AUTHENTICATION FAILED !! USE VALID DEVICE ID","REQUEST_ID": 0})
    #     response.status_code = 403
    #     return response
    # << -- END -->>

    lock.acquire()
    deviceId = request.args.get('deviceId')
    if (None == deviceId):
        resp = jsonify(
            {"PROFILE_URL": "please provide deviceId", "REQUEST_ID": 0})
        resp.status_code = 200
        return resp
    response = payload.fetchPayload(deviceId)
    lock.release()
    return response


@app.route('/upload', methods=['POST'])
def uploadFileToS3():
    return fileOperations.uploadfile()


@app.route('/uploadProfile', methods=['POST'])
def uploadProfile():
    """
    for uploading the file in s3 and do the post processing
    :return: json data containing success or failure
    """
    requestId = request.args.get('requestId')
    return service.processAndUploadProfile(requestId)


@app.route('/companyCrawlComplete', methods=['POST'])
def companyPostProcessing():
    '''
    for performing post processing of companies and getting all attributes
    '''
    requestId = request.args.get('requestId')
    return service.mergeCompanyAndProfileData(requestId)


if __name__ == '__main__':
    service.makeDirectories()
    scheduler = BackgroundScheduler()
    scheduler.add_job(func=scheduleCompanyCrawl.startCompanyCrawlForScheduledRequests, trigger="interval", minutes=10)
    scheduler.add_job(func=automatePost.hitApi, trigger="interval", minutes=40)
    scheduler.start()
    app.run(host=config.HOST, port=config.PORT)

    # <-- for auth check run -->
    # app.run(host="0.0.0.0", port=5000, ssl_context='adhoc')
