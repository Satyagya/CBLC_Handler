import boto3
import constants
import config
import logging

def auth_lookup(key):
    """
    Does authentication for the provided device Id
    """
    client = boto3.client(constants.S3)
    response=None
    try:
        response = client.list_objects_v2(Bucket=config.BUCKET_NAME, Prefix=key)
    except Exception as e:
        logging.error('Exception occurred while authentication lookup for key : ', key)
    for obj in response.get(constants.CONTENTS, []):
        if obj[constants.KEY] == key:
            return True
    return False