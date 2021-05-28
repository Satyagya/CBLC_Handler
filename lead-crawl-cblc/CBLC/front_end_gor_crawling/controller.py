from flask import Flask, render_template, jsonify, request, redirect, url_for, abort, send_from_directory, send_file

import appService
import threading
import constants
import csv_test
import re


app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

def helperGenerateLeads(fileName, email, stages):
    resultFile =  appService.start(fileName, email, stages)

def is_valid_email(email):
    regex = '^(\w|\.|\_|\-)+[@](\w|\_|\-|\.)+[.]\w{2,3}$'
    if(re.search(regex, email)):
        return True
    else:
        return False

@app.route('/startCrawl', methods=['POST'])
def startCrawl():
    uploadedFile = request.files['file']
    file_name = uploadedFile.filename
    email = ''
    stage = ''
    email = request.form['email']


    if email != '' and not is_valid_email(email):
        return render_template('error.html', response=constants.INVALID_EMAIL_RESPONSE, filename=file_name)

    try:
        stage = request.form['stages']
    except:
        stage = ''


    if email == '':
        return render_template('error.html', response=constants.EMPTY_EMAIL_RESPONSE, filename=file_name)
    elif stage == '':
        return render_template('error.html', response=constants.EMPTY_STAGE_RESPONSE, filename=file_name)

    response = appService.saveFileToLocal(uploadedFile)
    filename = response['filename']
    

    print(response['filename'])
    if response['reason'] == constants.ALL_CONDITIONS_CORRECT:
        csv_filename = csv_test.convert_to_csv(filename, stage)
        t = threading.Thread(target=helperGenerateLeads, args=(csv_filename, email, stage,))
        t.start()
        return render_template('successful.html', filename=response['filename'])

    else:
        reason = response['reason']
        if reason == constants.EMPTY_FILENAME:
            return render_template('error.html', response=constants.EMPTY_FILENAME_RESPONSE)
        elif reason == constants.WRONG_FILE_EXTENSION:
            return render_template('error.html', response=constants.WRONG_FILE_EXTENSION_RESPONSE,
                                   filename=response['filename'])
        elif reason == constants.WRONG_HEADER:
            return render_template('error.html', response=constants.WRONG_HEADER,
                                   filename=response['filename'])

@app.route('/upload_success')
def uploadSuccesful():
    return render_template('successful.html')

@app.route('/upload_error')
def uploadError():
    return render_template('error.html')

if __name__ == '__main__':
    app.run(host='localhost', port=8092, debug=True)