import logging

import boto3

import config


def obj_last_modified(myobj):
    """
    Gets the last modified date of the file
    :param myobj:
    :return: Last Modified timestamp of file
    """
    return myobj['LastModified']


def fileWithLeastTimestamp():
    """
    Gets the file with the least timestamp
    :return: file with the least timestamp
    """
    s3client = boto3.client('s3')
    try:
        response = s3client.list_objects(Bucket=config.BUCKET_NAME, Prefix=config.PREFIX)
        print('resonpnse is: ', response)
    except Exception as e:
        logging.error(e)
        print(f'Unexpected error while listing the dir {config.BUCKET_NAME}:{config.PREFIX}')
    if ('Contents' in response):
        l = []
        for key in response['Contents']:
            l.append(key)
        sortedObjects = sorted(l, key=obj_last_modified, reverse=False)
        return sortedObjects[0]['Key']
    return None
