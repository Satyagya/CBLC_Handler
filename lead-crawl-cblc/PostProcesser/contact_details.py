# import bingcrawler.config as config

#all definition should have functional comments
import config_Xpath
class ContactDetails:
    def __init__(self):
        email = None
        phone = None
        website = None
        profile_url = None

    def set_contact_details(self, response):
        self.set_email(response)
        self.set_phone(response)
        self.set_website(response)
        self.set_profile_url(response)

    def set_email(self, response):
        self.email = response.xpath(config_Xpath.CONTACT_INFO_EMAIL_XPATH).extract_first()

    def set_phone(self, response):
        self.phone = response.xpath(config_Xpath.CONTACT_INFO_PHONE_XPATH).extract_first()

    def set_website(self, response):
        self.website = response.xpath(config_Xpath.CONTACT_INFO_WEBSITE_XPATH).extract_first()

    def set_twitter(self, response):
        self.twitter = response.xpath(config_Xpath.CONTACT_INFO_TWITTER_XPATH).extract_first()

    def set_profile_url(self, response):
        self.profile_url = response.xpath(config_Xpath.CONTACT_INFO_URL_XPATH).extract_first()

    def get_email(self):
        return self.email

    def get_phone(self):
        return self.phone

    def get_website(self):
        return self.website

    def get_twitter(self):
        return self.twitter

    def get_profile_url(self):
        return self.profile_url
