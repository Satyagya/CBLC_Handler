import logging
from os import path

import boto3
import botocore
from botocore.exceptions import ClientError



def delete_object(bucket_name, object_name):
    """Delete an object from an S3 bucket

    :param bucket_name: string
    :param object_name: string
    :return: True if the referenced object was deleted, otherwise False
    """

    # Delete the object
    s3 = boto3.client('s3')
    try:
        s3.delete_object(Bucket=bucket_name, Key=object_name)
        print('Deleted')
    except ClientError as e:
        logging.error(e)
    except Exception as e:
        logging.error(e)
        print('delete failed')
        raise
    return True


def copy_object(src_bucket_name, src_object_name,
                dest_bucket_name, dest_object_name=None):
    """Copy an Amazon S3 bucket object

    :param src_bucket_name: string
    :param src_object_name: string
    :param dest_bucket_name: string. Must already exist.
    :param dest_object_name: string. If dest bucket/object exists, it is
    overwritten. Default: src_object_name
    :return: True if object was copied, otherwise False
    """

    # Construct source bucket/object parameter
    copy_source = {'Bucket': src_bucket_name, 'Key': src_object_name}
    if dest_object_name is None:
        dest_object_name = src_object_name

    # Copy the object
    s3 = boto3.client('s3')
    try:
        s3.copy_object(CopySource=copy_source, Bucket=dest_bucket_name,
                       Key=dest_object_name)
    except ClientError as e:
        logging.error(e)
        raise
    return True


def move_object(src_bucket_name, src_object_name,
                dest_bucket_name, dest_object_name=None):
    """Copy an Amazon S3 bucket object

    :param src_bucket_name: string
    :param src_object_name: string
    :param dest_bucket_name: string. Must already exist.
    :param dest_object_name: string. If dest bucket/object exists, it is
    overwritten. Default: src_object_name
    :return: True if object was copied, otherwise False
    """
    try:
        success = copy_object(src_bucket_name, src_object_name,
                              dest_bucket_name, dest_object_name)
        if success:
            delete_object(src_bucket_name, src_object_name)

        print('{} Deleted'.format(src_object_name))
    except Exception as e:
        logging.error(e)
        print(
            f'Moving failed {src_bucket_name}:{src_object_name} to {dest_bucket_name}:{dest_object_name}')
        raise


def download_file(BUCKET_NAME='company-html', object_path='IN/tempo.txt', local_path=None):
    # BUCKET_NAME replace with your bucket name
    # KEY=PATH replace with your object key
    s3 = boto3.resource('s3')
    if local_path is None:
        local_path = path.basename(object_path)
    try:
        print("object_path", object_path)
        s3.Bucket(BUCKET_NAME).download_file(object_path, local_path)
        
        return True
    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == "404":
            print("The object does not exist.")
            return False
        else:
            raise
    except Exception as e:
        logging.error(e)
        print(f'Download failed {BUCKET_NAME}: {object_path}')
        raise

def upload_file(bucket='company-html', file_name='local_path', object_name='s3_path'):
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
