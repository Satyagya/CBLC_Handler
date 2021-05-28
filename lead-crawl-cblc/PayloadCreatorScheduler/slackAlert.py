import slack
import config

TOKEN=config.SLACK_TOKEN
CHANNEL=config.SLACK_CHANNEL

def sendNotification(message):
    client = slack.WebClient(TOKEN, timeout=30)
    client.chat_postMessage(channel=CHANNEL,text=message,username=config.SLACK_USERNAME,icon_emoji=config.SLACK_EMOJI)



