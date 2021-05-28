import logging
from os import path

import boto3
import botocore
from botocore.exceptions import ClientError
logging.basicConfig(filename='app.log', filemode='w', format='%(name)s - %(levelname)s - %(message)s')

def download_file(BUCKET_NAME, object_path, local_path=None):
    """
    Downloads the file from the specified bucket's path
    :param BUCKET_NAME: bucket name from which file needs to be downloaded
    :param object_path: path of file in s3
    :param local_path: local path for file
    :return:
    """
    s3 = boto3.resource('s3')
    if local_path is None:
        local_path = path.basename(object_path)
    try:
        s3.Bucket(BUCKET_NAME).download_file(object_path, local_path)
        return True
    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == "404":
            print("The object does not exist.")
            logging.exception(e)
            return False
        else:
            logging.exception(e)
            raise
    except Exception as e:
        logging.exception(e)
        print(f'Download failed {BUCKET_NAME}: {object_path}')
        raise


def upload_file(bucket, file_name, object_name):
    """Upload a file to an S3 bucket
    :param bucket: Bucket to upload to
    :param file_name: File to upload
    :param object_name: S3 object name. If not specified then same as file_name
    :return: True if file was uploaded, else False
    """
    # If S3 object_name was not specified, use file_name
    if object_name is None:
        object_name = file_name
    # Upload the file
    s3_client = boto3.client('s3')
    try:
        response = s3_client.upload_file(file_name, bucket, object_name)
    except ClientError as e:
        logging.error(e)
        raise
    except Exception as e:
        logging.error(e)
        print('Upload failed error')
        raise
    return True
