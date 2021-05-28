from flask import Flask, render_template, jsonify, request, redirect, url_for, abort, send_from_directory, send_file

import config
import appService
import constants
import fetchProduct

app = Flask(__name__)


@app.route('/')
def index():
    data = fetchProduct.getProductsFromDB()
    return render_template('index.html', data = data)


@app.route('/upload_success')
def uploadSuccesful():
    data = fetchProduct.getProductsFromDB()
    return render_template('successful.html', data=data)


@app.route('/download/<filename>', methods=['GET'])
def downloadFile(filename):
    # filename = request.args['filename']
    return send_file(config.OUTPUT_FILES+filename, as_attachment=filename)


@app.route('/upload_error')
def uploadError():
    data = fetchProduct.getProductsFromDB()
    return render_template('error.html', data=data)


@app.route('/uploadCsvFile', methods=['POST'])
def uploadCsvFile():
    uploaded_file = request.files['file']
    productSelected = request.form.get('product_types')

    response = appService.saveFileToLocalAndUploadToS3(uploaded_file, productSelected)
    if response['response'] == True and response['reason'] == constants.ALL_CONDITIONS_CORRECT:
        return render_template('successful.html', filename=response['filename'], data=response['productList'])
    else:
        reason = response['reason']
        if reason == constants.EMPTY_FILENAME:
            return render_template('error.html', response=constants.EMPTY_FILENAME_RESPONSE, data=response['productList'])
        elif reason == constants.WRONG_FILE_EXTENSION:
            return render_template('error.html', response=constants.WRONG_FILE_EXTENSION_RESPONSE,
                                   filename=response['filename'], data=response['productList'])
        elif reason == constants.UPLOAD_TO_S3_FAILED:
            return render_template('error.html', response=constants.UPLOAD_TO_S3_FAILED_RESPONSE,
                                   filename=response['filename'], data=response['productList'])
        elif reason == constants.PRODUCT_NOT_SELECTED:
            return render_template('error.html', response=constants.PRODUCT_NOT_SELECTED_RESPONSE,
                                   filename=response['filename'], data=response['productList'])


if __name__ == '__main__':
    app.run(host=config.HOST, port=config.PORT)
