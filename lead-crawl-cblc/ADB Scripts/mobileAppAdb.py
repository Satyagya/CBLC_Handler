import datetime
import json
import logging
import os
import random
import shutil
import time

import boto3
from botocore.exceptions import ClientError


from ppadb.client import Client as AdbClient

MOBILE_PATH = '/sdcard/DCIM/MyAlbums/FlightMode/flight.txt'
MOBILE_PATH_SINGLE_TOGGLE = '/sdcard/DCIM/MyAlbums/FlightMode/flight1.txt'
MAKE_DIR = 'requests'
HOME_DIR = 'requests/'
HOST = '127.0.0.1'
PORT = 5037
APP_RELAUNCH = 'am start -n com.example.webload/com.example.webload.IntermediateActivity'
REMOVE_FILE = 'rm /sdcard/DCIM/MyAlbums/FlightMode/flight.txt'
STOP_APP = 'am force-stop com.example.webload'
SLEEP_FILE = '/sdcard/DCIM/MyAlbums/sleep/sleep.zip'
deviceSerialUserAgents = {
    'HNJ035GF': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/600.2.5 (KHTML, like Gecko) Version/8.0.2 Safari/600.2.5'}
deviceSerialIp={'HNJ035GF':'13.234.114.169:5000'}
deviceSerialReferer={'HNJ035GF':'Google'}
deviceSerialAirplaneToggle={'HNJ035GF':'am start -a android.settings.AIRPLANE_MODE_SETTINGS && input tap 645 1045 && sleep 5 &&input tap 645 1045 &&clear com.android.settings'}
deviceSerialSingleAirplaneToggle={'HNJ035GF':'am start -a android.settings.AIRPLANE_MODE_SETTINGS && input tap 645 1045 && sleep 5 &&clear com.android.settings'}
USERAGENT_FILE = '/sdcard/DCIM/useragent.txt'
LOCAL_USERAGENT_FILE = 'useragent.txt'
CONFIG_IP_FILE='/sdcard/DCIM/config/ip.txt'
LOCAL_CONFIG_IP_FILE='ip.txt'
CONFIG_REFERRER_FILE='/sdcard/DCIM/config/referrer.txt'
LOCAL_REFERRER_FILE='referrer.txt'
BUCKET_NAME = 'company-crawler-check'




def checkAndUpload(device):
    DEVICE_ID = str(device.serial)
    UPDATE_CHECK = '/sdcard/DCIM/upload.txt'
    checkResponse = device.pull(UPDATE_CHECK, 'uploadCheck.txt')
    if None is checkResponse:
        try:
            print("File found for s3 upload...")
            PULL_PATH = 'adb -s ' + str(DEVICE_ID) + ' pull /sdcard/DCIM/upload/'
            localPath = 'crawled'
            response = os.system(PULL_PATH + ' ' + localPath)
            randomRequestId = random.randrange(90000, 9000000, 1)
            localZipPath = str(randomRequestId)
            shutil.make_archive(localZipPath, 'zip', localPath)
            zipFile = localZipPath + '.zip'
            upload_file(BUCKET_NAME, zipFile, 'MOBILE_CRAWLED_ZIPS/CRAWLED_' + zipFile)
            device.shell('rm -r /sdcard/DCIM/upload')
            device.shell('rm -r /sdcard/DCIM/upload.txt')
            shutil.rmtree('crawled')
            print(zipFile)
            # os.remove(zipFile)   
        except Exception as e:
            print("Exception occurred while uploadin to s3!!")
            print(e)
    else:
        print("No files found for s3 upload")


def upload_file(bucket='company-html', file_name='local_path', object_name='s3_path'):
    if object_name is None:
        object_name = file_name
    # Upload the file
    s3_client = boto3.client('s3')
    try:
        response = s3_client.upload_file(file_name, bucket, object_name)
        print("File successfully uploaded to s3")
    except ClientError as e:
        logging.error(e)
        raise
    except Exception as e:
        logging.error(e)
        print('Upload failed error')
        raise
    return True

def getRandomUAAndReferer(prevRes):
    res=None
    with open('ua.json') as f:
        data=json.load(f)
        while True:
            res = random.choice(data)
            if(prevRes!=res):
                break
            else:
                print('user agent and referer repeated --- Handled')
    prevRes = res
    return prevRes

def sendSingleAdbCommand(device, devicePath):
    deviceSerial = str(device.serial)
    os.remove(HOME_DIR + str(devicePath))
    device.shell(STOP_APP)
    device.shell(deviceSerialSingleAirplaneToggle[deviceSerial])
    print("Airplane Mode successfully toggled (Single)")
    device.shell(REMOVE_FILE)
    device.shell('run-as com.example.webload rm -r /data/data/com.example.webload/app_webview')
    time.sleep(4)
    device.shell(APP_RELAUNCH)
    print("App Launched Successfully")
    time.sleep(3)
    print("DeviceId : " + str(device.serial) + " Device Path: " + str(devicePath) + " Time:" + str(
        datetime.datetime.now()))


def sendAdbCommand(device, devicePath):
    deviceSerial = str(device.serial)
    os.remove(HOME_DIR + str(devicePath))
    device.shell(STOP_APP)
    device.shell(deviceSerialAirplaneToggle[deviceSerial])
    print("Airplane Mode successfully toggled")
    device.shell(REMOVE_FILE)
    device.shell('run-as com.example.webload rm -r /data/data/com.example.webload/app_webview')
    time.sleep(4)
    device.shell(APP_RELAUNCH)
    print("App Launched Successfully")
    time.sleep(3)
    print("DeviceId : " + str(device.serial) + " Device Path: " + str(devicePath) + " Time:" + str(
        datetime.datetime.now()))

def writeUseragent(device,useragent):
    with open(LOCAL_USERAGENT_FILE, 'w') as f:
        f.write(useragent)
    device.push(LOCAL_USERAGENT_FILE, USERAGENT_FILE)
    print("User Agent changed for device",device.serial)
    print("New User Agent is: ",useragent)

def writeReferer(device,referer):
    with open(LOCAL_REFERRER_FILE, 'w') as f:
        f.write(referer)
    device.push(LOCAL_REFERRER_FILE, CONFIG_REFERRER_FILE)
    print("Referer changed for device", device.serial)
    print("New Referrer is : ",referer)

def toggleAirplaneMode():
    prevRandom=None
    while True:
        time.sleep(2)
        client = AdbClient(host=HOST, port=PORT)
        devices = client.devices()
        for device in devices:
            deviceSerial = str(device.serial)
            if (deviceSerial in deviceSerialUserAgents.keys()):
                checkAndUpload(device)
                try:
                    if not os.path.exists(HOME_DIR):
                        os.mkdir(MAKE_DIR)
                    devicePath = device.get_device_path()
                    doubleToggle = device.pull(MOBILE_PATH, HOME_DIR + str(devicePath))
                    singleToggle = device.pull(MOBILE_PATH_SINGLE_TOGGLE, HOME_DIR + str(devicePath))
                    if (None is doubleToggle):
                        sendAdbCommand(device, devicePath)
                        prevRandom=getRandomUAAndReferer(prevRandom)
                        referer= list(prevRandom.keys())[0]
                        userAgent = list(prevRandom.values())[0]
                        writeReferer(device,referer)
                        writeUseragent(device,userAgent)
                    elif (None is singleToggle):
                        sendSingleAdbCommand(device, devicePath)
                        prevRandom = getRandomUAAndReferer(prevRandom)
                        referer = list(prevRandom.keys())[0]
                        userAgent = list(prevRandom.values())[0]
                        writeReferer(device, referer)
                        writeUseragent(device, userAgent)
                    else:
                        print("Airplane Mode not toggled : File doesn't exist")
                except Exception as e:
                    print(":: Exception occurred while toggling airplane mode ::")
                    print(e)


def configureUserAgent():
    client = AdbClient(host=HOST, port=PORT)
    devices = client.devices()
    for device in devices:
        deviceSerial = str(device.serial)
        print('Configuring user agent for device :' + deviceSerial)
        if deviceSerial in deviceSerialUserAgents.keys():
            with open(LOCAL_USERAGENT_FILE, 'w') as f:
                f.write(deviceSerialUserAgents[deviceSerial])
            device.push(LOCAL_USERAGENT_FILE, USERAGENT_FILE)

def configureIp():
    client = AdbClient(host=HOST, port=PORT)
    devices = client.devices()
    for device in devices:
        deviceSerial = str(device.serial)
        print('Configuring IP for device :' + deviceSerial)
        if deviceSerial in deviceSerialIp.keys():
            with open(LOCAL_CONFIG_IP_FILE, 'w') as f:
                f.write(deviceSerialIp[deviceSerial])
            device.push(LOCAL_CONFIG_IP_FILE, CONFIG_IP_FILE)

def configureReferrer():
    client = AdbClient(host=HOST, port=PORT)
    devices = client.devices()
    for device in devices:
        deviceSerial = str(device.serial)
        print('Configuring referer for device :' + deviceSerial)
        if deviceSerial in deviceSerialReferer.keys():
            with open(LOCAL_REFERRER_FILE, 'w') as f:
                f.write(deviceSerialReferer[deviceSerial])
            device.push(LOCAL_REFERRER_FILE, CONFIG_REFERRER_FILE)




configureUserAgent()
configureIp()
configureReferrer()
toggleAirplaneMode()
