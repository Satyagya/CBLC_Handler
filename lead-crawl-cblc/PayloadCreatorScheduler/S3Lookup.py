import logging

import boto3

import config

logging.basicConfig(filename='app.log', filemode='w', format='%(name)s - %(levelname)s - %(message)s')


def fast_lookup(key):
    """
    s3 lookup for duplicates
    :param key:
    :return:
    """
    try:
        client = boto3.client('s3')
        response = client.list_objects_v2(Bucket=config.BUCKET_NAME, Prefix=key)
        for obj in response.get('Contents', []):
            if obj['Key'] == key:
                return True
        client.put_object(Bucket=config.BUCKET_NAME, Key=key)
        return False
    except Exception as e:
        logging.error(e)
    return False


def checkDuplicate(name):
    """
    checks for the duplicates
    :param name:
    :return:
    """
    key = config.LOOKUP_FOLDER_PATH + str(name) + config.JSON_EXTENSION
    return fast_lookup(key)
