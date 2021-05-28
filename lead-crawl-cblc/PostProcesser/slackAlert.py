import slack
import config

TOKEN=config.SLACK_TOKEN
CHANNEL=config.SLACK_CHANNEL
BOT_USERNAME = config.SLACK_BOT_USERNAME
BOT_EMOJI = config.SLACK_BOT_EMOJI


def sendNotification(message):
    client = slack.WebClient(TOKEN)
    client.chat_postMessage(channel=CHANNEL,text=message,username=BOT_USERNAME,icon_emoji=BOT_EMOJI)

