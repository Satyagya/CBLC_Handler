import S3operations
import constants
import os
import delete
import threading
import fetchProduct

lock = threading.Lock()


def saveFileToLocalAndUploadToS3(uploaded_file, productSelected):

    response = False
    responseDict = dict()
    
    filename = uploaded_file.filename
    filename_extension = filename.split('.')[-1]
    filename_without_extension = filename.split('.')[0]
    filename_without_extension = filename_without_extension.replace(" ", "-")
    filename_without_extension = filename_without_extension.replace("_", "-")
    if productSelected==constants.NULL:
        reason = constants.PRODUCT_NOT_SELECTED
    if not filename_extension in constants.FILE_EXTENSION:
        reason = constants.WRONG_FILE_EXTENSION
    if filename=='':
        reason=constants.EMPTY_FILENAME


    if filename != '' and filename_extension in constants.FILE_EXTENSION and productSelected!=constants.NULL:
        if not os.path.exists(os.getcwd() + constants.SLASH + constants.FILES_DIRECTORY):
            os.mkdir(constants.FILES_DIRECTORY)
        destinationToSaveFile = os.path.join(
            os.getcwd(), constants.FILES_DIRECTORY+constants.SLASH+filename)
        uploaded_file.save(destinationToSaveFile)
        if os.path.isfile(destinationToSaveFile):
            newfilename = filename_without_extension + constants.UNDERSCORE + productSelected + constants.UNDERSCORE +  \
                str(getRequestID())+constants.CSV_EXTENSION
            response = uploadFileToS3(destinationToSaveFile, newfilename)
            delete.deleteDownloadedFile(filename)
            if response==False:
                reason = constants.UPLOAD_TO_S3_FAILED
            elif response==True:
                reason = constants.ALL_CONDITIONS_CORRECT
                
    productList = fetchProduct.getProductsFromDB()
    responseDict['filename'] = filename
    responseDict['response'] = response
    responseDict['reason'] = reason
    responseDict['productList'] = productList
    return responseDict


def uploadFileToS3(filePath, filename):
    response = None
    try:
        response = S3operations.upload_file(
            constants.BUCKET, filePath, constants.BUCKET_FOLDER_PATH+constants.SLASH+filename)
    except Exception as e:
        print(e)

    return response


def getRequestID():

    lock.acquire()
    requestId = None
    with open(constants.REQUEST_ID_PATH, 'r+') as file:
        data = file.readlines()
        for i in data:
            requestId = int(i)
            print(requestId)
            break
        file.seek(0)
        file.truncate()
        newRequestId = requestId + 1
        file.write(str(newRequestId))
        lock.release()
        return requestId
