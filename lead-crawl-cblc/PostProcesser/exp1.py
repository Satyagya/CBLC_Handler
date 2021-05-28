import config_Xpath
import time
from datetime import datetime
from dateutil.parser import parse

import os
print (os.getcwd())
import pandas as pd
df = pd.read_csv(config_Xpath.CSV_PATH)
companygroupstatus = 0
company = ""
print("companygroupstatus is in start:",companygroupstatus)

class ExperienceDetails:
    def __init__(self):
        self.start_year = None
        self.end_year = None
        self.designation = None
        self.industry_type = None
        self.function = None
        self.duration = None
        self.company = None
        self.company_size = None
        self.seniority_level = None
        self.is_current = None

    def set_experience_details(self, response):
        self.set_designation(response)
        self.set_start_year(response)
        self.set_end_year(response)
        self.set_industry_type(response)
        self.set_duration(response)
        self.set_company(response)
        self.set_company_size(response)
        self.set_is_current(response)


    def set_company(self, response):
        global companygroupstatus
        x1 = response.xpath(config_Xpath.COMPANY_ALT_TITLE_XPATH).extract_first()
        print("first x1 is :",x1)
        if x1:
            if 'tracking' in x1:
                t = x1.split('</a')
                t1 = t[0].split('>')[-1]
                self.company = t1
            else:
                t = x1.split('>')
                t1 = t[1].split('<')[0]
                self.company = t1
        if x1 is None:
            print("I came into x2")
            x2 = response.xpath(config_Xpath.nameofcompanyxpath).extract()
            companygroupstatus = 1
            print("x2 is now:", x2)
            if x2:
                t = x2[0].split('>')
                t1 = t[1].split('<')[0]
                self.company = t1
        print("companygroupstatus in set_company is :",companygroupstatus)



    def set_start_year(self, response):
        print("companygroupstatus in srat yr:",companygroupstatus)
        if companygroupstatus == 0:
            y = response.xpath(config_Xpath.EMPLOYMENT_START_DATE_XPATH).extract_first()
            if(y):
                x = y
                t = x.split(' ')
                x1 = t[-1]
                date_time_obj = datetime.strptime(x1, '%Y')
                x = date_time_obj.date()
                y1 = str(x)
                y2 = time.mktime(datetime.strptime(y1, "%Y-%m-%d").timetuple())
                y3 = str(y2)
                self.start_year = y3
            else:
                self.start_year = None
        if companygroupstatus == 1:
            startxpath = ".//*[contains(@class, 'experience-group-position__duration experience-group-position__meta-item')]/span/time[@class='date-range__start-date']/text()"
            y = response.xpath(startxpath).extract_first()
            if (y):
                x = y
                t = x.split(' ')
                x1 = t[-1]
                date_time_obj = datetime.strptime(x1, '%Y')
                x = date_time_obj.date()
                y1 = str(x)
                y2 = time.mktime(datetime.strptime(y1, "%Y-%m-%d").timetuple())
                y3 = str(y2)
                self.start_year = y3
            else:
                self.start_year = None

    def set_end_year(self, response):
        print("companygroupstatus in end yr:",companygroupstatus)
        if companygroupstatus == 0:
            y = response.xpath(config_Xpath.EMPLOYMENT_END_DATE_XPATH).extract_first()
            if y == None:
                self.end_year = 'Present'
            else:
                x = y
                t = x.split(' ')
                x1 = t[-1]
                date_time_obj = datetime.strptime(x1, '%Y')
                x = date_time_obj.date()
                y1 = str(x)
                y2 = time.mktime(datetime.strptime(y1, "%Y-%m-%d").timetuple())
                y3 = str(y2)
                self.end_year = y3
        if companygroupstatus == 1:
            endxapth = ".//*[contains(@class, 'experience-group-position__duration experience-group-position__meta-item')]/span/time[@class='date-range__end-date']/text()"
            y = response.xpath(endxapth).extract_first()
            if y == None:
                self.end_year = 'Present'
            else:
                x = y
                t = x.split(' ')
                x1 = t[-1]
                date_time_obj = datetime.strptime(x1, '%Y')
                x = date_time_obj.date()
                y1 = str(x)
                y2 = time.mktime(datetime.strptime(y1, "%Y-%m-%d").timetuple())
                y3 = str(y2)
                self.end_year = y3




    def set_industry_type(self, response):
        self.industry_type = None

    def set_company_size(self, response):
        self.company_size = None



#recent code

    # def set_duration(self, response):
    #     global companygroupstatus
    #     print("companygroupstatus is :", companygroupstatus)
    #     if companygroupstatus == 0:
    #         x = response.xpath(config_Xpath.EMPLOYMENT_DURATION_XPATH).extract_first()
    #         print("durn:",x)
    #         if x:
    #             t11 = x.split(' ')
    #             ts = ''
    #             if len(t11) == 4:
    #                 if t11[0].isdigit():
    #                     durationint = int(t11[0]) * 12
    #                     if t11[2].isdigit():
    #                         durationint = durationint + int(t11[2])
    #                         ts = str(durationint)
    #                         self.duration = ts
    #             if len(t11) == 2:
    #                 if t11[1].lower().startswith("mo"):
    #                     if t11[0].isdigit():
    #                         totalduration = int(t11[0])
    #                         ts = str(totalduration)
    #                         self.duration = ts
    #                 if t11[1].lower().startswith("y"):
    #                     if t11[0].isdigit():
    #                         totalduration = int(t11[0])*12
    #                         ts = str(totalduration)
    #                         self.duration = ts
    #         else:
    #             self.duration = 0
    #
    #         print("Final duration is",self.duration)
    #     if companygroupstatus == 1:
    #         groupdurationxpath = ".//*[contains(@class, 'experience-group-header__duration')]/text()"
    #         self.duration = 0
    #         x = response.xpath(groupdurationxpath).extract_first()
    #         print("durn:", x)
    #         if x:
    #             t11 = x.split(' ')
    #             ts = ''
    #             if len(t11) == 4:
    #                 if t11[0].isdigit():
    #                     durationint = int(t11[0]) * 12
    #                     if t11[2].isdigit():
    #                         durationint = durationint + int(t11[2])
    #                         ts = str(durationint)
    #                         self.duration = ts
    #             if len(t11) == 2:
    #                 if t11[1].lower().startswith("mo"):
    #                     if t11[0].isdigit():
    #                         totalduration = int(t11[0])
    #                         ts = str(totalduration)
    #                         self.duration = ts
    #                 if t11[1].lower().startswith("y"):
    #                     if t11[0].isdigit():
    #                         totalduration = int(t11[0]) * 12
    #                         ts = str(totalduration)
    #                         self.duration = ts
    #
    #         else:
    #             self.duration = 0

    def set_duration(self, response):
        global companygroupstatus
        print("companygroupstatus is :", companygroupstatus)
        if companygroupstatus == 0:
            x = response.xpath(config_Xpath.EMPLOYMENT_DURATION_XPATH).extract_first()
            print("durn:",x)
            if x:
                t11 = x.split(' ')
                ts = ''
                if len(t11) == 4:
                    if t11[0].isdigit():
                        durationint = int(t11[0]) * 12
                        if t11[2].isdigit():
                            durationint = durationint + int(t11[2])
                            ts = str(durationint)
                            self.duration = ts
                if len(t11) == 2:
                    if t11[1].lower().startswith("mo"):
                        if t11[0].isdigit():
                            totalduration = int(t11[0])
                            ts = str(totalduration)
                            self.duration = ts
                    if t11[1].lower().startswith("y"):
                        if t11[0].isdigit():
                            totalduration = int(t11[0])*12
                            ts = str(totalduration)
                            self.duration = ts
            else:
                self.duration = 0

            print("Final duration is",self.duration)
        if companygroupstatus == 1:
            groupdurationxpath = ".//*[contains(@class, 'experience-group-position__duration experience-group-position__meta-item')]/span/span/text()"
            self.duration = 0
            x = response.xpath(groupdurationxpath).extract_first()
            print("durn:", x)
            if x:
                t11 = x.split(' ')
                ts = ''
                if len(t11) == 4:
                    if t11[0].isdigit():
                        durationint = int(t11[0]) * 12
                        if t11[2].isdigit():
                            durationint = durationint + int(t11[2])
                            ts = str(durationint)
                            self.duration = ts
                if len(t11) == 2:
                    if t11[1].lower().startswith("mo"):
                        if t11[0].isdigit():
                            totalduration = int(t11[0])
                            ts = str(totalduration)
                            self.duration = ts
                    if t11[1].lower().startswith("y"):
                        if t11[0].isdigit():
                            totalduration = int(t11[0]) * 12
                            ts = str(totalduration)
                            self.duration = ts

            else:
                self.duration = 0





    # def set_designation(self, response):
    #     global companystatus
    #     i = 0
    #     employeeprofilexpath = ".//*[contains(@class, 'result-card__title experience-group-position__title')]/text()"
    #
    #
    #     designation_typical = response.xpath(config_Xpath.EMPLOYEE_PROFILE_XPATH).extract()
    #     print("designation is:",designation_typical)
    #     if len(designation_typical) == 0:
    #         self.designation = response.xpath(employeeprofilexpath).extract()
    #         self.seniority_level = None
    #         self.function = None
    #     else:
    #         if len(designation_typical) == 1:
    #             self.designation = designation_typical[0]
    #         else:
    #             self.designation = designation_typical
    #         # self.designation = designation_typical[0]
    #         self.seniority_level = None
    #         self.function = None


    # def set_designation(self, response):
    #     global companygroupstatus
    #     i = 0
    #     employeeprofilexpath = ".//*[contains(@class, 'result-card__title experience-group-position__title')]/text()"
    #     designation_rcvd = response.xpath(employeeprofilexpath).extract()
    #     if len(designation_rcvd) >= 0:
    #         companygroupstatus = 1
    #         print("companygroupstatus is :",companygroupstatus)
    #     print("designation_rcvd is :",designation_rcvd)
    #     self.designation = designation_rcvd
    #     self.seniority_level = None
    #     self.function = None
    #     if len(designation_rcvd) == 0:
    #         designation_typical = response.xpath(config_Xpath.EMPLOYEE_PROFILE_XPATH).extract()
    #         print("designation is:",designation_typical)
    #         self.designation = designation_typical[0]
    #         self.seniority_level = None
    #         self.function = None

#recent designation code here
    # def set_designation(self, response):
    #     global companygroupstatus
    #     i = 0
    #     employeeprofilexpath = ".//*[contains(@class, 'result-card__title experience-group-position__title')]/text()"
    #     designation_rcvd = response.xpath(employeeprofilexpath).extract()
    #     if len(designation_rcvd) > 0:
    #         companygroupstatus = 1
    #         print("companygroupstatus is :",companygroupstatus)
    #     print("designation_rcvd is :",designation_rcvd)
    #     self.designation = designation_rcvd
    #     self.seniority_level = None
    #     self.function = None
    #     if len(designation_rcvd) == 0:
    #         designation_typical = response.xpath(config_Xpath.EMPLOYEE_PROFILE_XPATH).extract()
    #         print("designation is:",designation_typical)
    #         self.designation = designation_typical[0]
    #         self.seniority_level = None
    #         self.function = None


    def set_designation(self, response):
        global companygroupstatus
        i = 0
        employeeprofilexpath = ".//*[contains(@class, 'result-card__title experience-group-position__title')]/text()"
        designation_rcvd = response.xpath(employeeprofilexpath).extract_first()
        if designation_rcvd is not None:
            companygroupstatus = 1
            print("companygroupstatus is :",companygroupstatus)
        print("designation_rcvd is :",designation_rcvd)
        self.designation = designation_rcvd
        self.seniority_level = None
        self.function = None
        if designation_rcvd is None:
            designation_typical = response.xpath(config_Xpath.EMPLOYEE_PROFILE_XPATH).extract()
            print("designation is:",designation_typical)
            self.designation = designation_typical[0]
            self.seniority_level = None
            self.function = None





    def set_is_current(self, response):
        if self.get_end_year() == 'Present':
            self.is_current = True
        else:
            self.is_current = False

    def get_company(self):
        return self.company


    def get_start_year(self):
        return self.start_year

    def get_end_year(self):
        return self.end_year

    def get_duration(self):
        return self.duration

    def get_designation(self):
        return self.designation

    def get_industry_type(self):
        return self.industry_type

    def get_function(self):
        return self.function

    def get_company_size(self):
        return self.company_size

    def get_seniority_level(self):
        return self.seniority_level

    def get_is_current(self):
        return self.is_current







