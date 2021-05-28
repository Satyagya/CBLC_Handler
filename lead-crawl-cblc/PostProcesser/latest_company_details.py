import config_Xpath
class LatestCompanyDetails:
    def __init__(self):
        website = None
        industry_type = None
        company_size = None

    def set_latest_company_details(self, response):
        self.set_website(response)
        self.set_industry_type(response)
        self.set_company_size(response)

    def set_website(self, response):
        self.website = response.xpath(config_Xpath.COMPANY_ABOUT_WEBSITE).extract_first()

    def set_industry_type(self, response):
        self.industry_type = response.xpath(config_Xpath.COMPANY_INDUSTRY_TYPE).extract_first()

    def set_company_size(self, response):
        self.company_size = response.xpath(config_Xpath.COMPANY_SIZE).extract_first()

    def get_website(self):
        return self.website

    def get_industry_type(self):
        return self.industry_type

    def get_company_size(self):
        return self.company_size
