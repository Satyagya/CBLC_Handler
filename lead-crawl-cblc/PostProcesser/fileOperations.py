import os
from flask import request, jsonify
from werkzeug.utils import secure_filename

import config
import jsonHandler
import s3Operations


def uploadfile():
    """
    uploads the file to s3
    :return:
    """

    if request.method == 'POST':
        # check if the post request has the file part
        if 'file' not in request.files:
            resp = jsonify({'message': 'No file in the request'})
            resp.status_code = 400
            return resp
        file = request.files['file']
        if file.filename == '':
            resp = jsonify({'message': 'No file selected for uploading'})
            resp.status_code = 400
            return resp
        if file:
            filename = secure_filename(file.filename)
            if not os.path.exists(config.REQUEST_DIR):
                os.mkdir(config.REQUEST_DIR)
            file.save(os.path.join(config.REQUEST_DIR, filename))
            requestId = jsonHandler.readJsonAndAddRequestId(os.path.join(config.REQUEST_DIR, filename))
            if (requestId):
                s3Operations.upload_file(config.BUCKET_NAME, config.REQUEST_PATH + filename,
                                         config.PREFIX + str(requestId))
                os.remove(config.REQUEST_PATH + filename)
                resp = jsonify({'message': 'File successfully uploaded :' + filename})
                resp.status_code = 201
                return resp
            resp = jsonify({'message': 'Failed to upload file'})
            resp.status_code = 400
            return resp
