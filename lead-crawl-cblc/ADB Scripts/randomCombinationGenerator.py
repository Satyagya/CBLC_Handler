import json
import random

REFERER_FILE='referer.txt'
USERAGENT_FILE='useragents.txt'
NEWLINE='\n'
BLANK=''
WRITE_MODE='w'
RANDOM_FILE='ua.json'

with open(REFERER_FILE) as f:
    referers=f.readlines()
    refererList=[]
    for referer in referers:
        refererList.append(referer.replace(NEWLINE,BLANK))
    random.shuffle(refererList)
with open(USERAGENT_FILE) as f:
    userAgents = f.readlines()
    userAgentsList = []
    for userAgent in userAgents:
        userAgentsList.append(userAgent.replace(NEWLINE, BLANK))
    random.shuffle(userAgentsList)

randomRefererAndUA=[]
for i in refererList:
    for j in userAgentsList:
        d = {}
        d[i]=j
        randomRefererAndUA.append(d)
random.shuffle(randomRefererAndUA)
with open(RANDOM_FILE,WRITE_MODE) as f:
    json.dump(randomRefererAndUA,f)

