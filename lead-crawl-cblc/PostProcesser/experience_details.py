import pandas as pd
import config_Xpath
import time
import html
from datetime import datetime
from dateutil.parser import parse
startstatus = 1
import os
print(os.getcwd())
df = pd.read_csv(config_Xpath.CSV_PATH)
companygroupstatus = 0

print("companygroupstatus is in start:", companygroupstatus)



class ExperienceDetails:
    def __init__(self):
        self.start_year = ''
        self.end_year = ''
        self.designation = ''
        self.industry_type = ''
        self.function = []
        self.duration = ''
        self.company = ''
        self.company_size = ''
        self.seniority_level = ''
        self.is_latest = ''

    def set_experience_details(self, response):
        self.set_start_year(response)
        self.set_end_year(response)
        self.set_designation(response)
        self.set_industry_type(response)
        self.set_duration(response)
        self.set_company(response)
        self.set_company_size(response)
        self.set_is_latest(response)

    def set_company(self, response):
        global companygroupstatus
        x1 = response.xpath(config_Xpath.COMPANY_ALT_TITLE_XPATH).extract_first()
        print("first x1 is :", x1)
        if x1:
            if 'tracking' in x1:
                t = x1.split('</a')
                t1 = t[0].split('>')[-1]
                if t1:
                    companystring = t1.encode(encoding='UTF-8', errors='strict').decode()
                    c1 = html.unescape(str(companystring))
                    self.company = c1.replace("'", "").replace('"', '')

            else:
                t = x1.split('>')
                t1 = t[1].split('<')[0]
                if t1:
                    companystring = t1.encode(encoding='UTF-8', errors='strict').decode()
                    c2 = html.unescape(str(companystring))
                    self.company = c2.replace("'", "").replace('"', '')
        if x1 is None:
            print("I came into x2")
            x2 = response.xpath(config_Xpath.nameofcompanyxpath).extract()
            companygroupstatus = 1
            print("x2 is now:", x2)
            if x2:
                t = x2[0].split('>')
                t1 = t[1].split('<')[0]
                if t1:
                    companystring = t1.encode(encoding='UTF-8', errors='strict').decode()
                    c3 = html.unescape(str(companystring))
                    self.company = c3.replace("'", "").replace('"', '')
        print("companygroupstatus in set_company is :", companygroupstatus)

    def set_start_year(self, response):
        global startstatus
        # startstatus = 1
        print("startstatus is in now decemberstart1:", startstatus)
        y = response.xpath(config_Xpath.EMPLOYMENT_START_DATE_XPATH).extract_first()
        print("y is  in set_start_year  ", y)
        if (y):
            startstatus = 1
            x = y
            print(y)
            print("*******************start year*****************8")
            # input()

            # code on 17/02/20 start
            from dateutil.parser import parse
            try:
                dt = parse(y)
                print(dt)
                print(dt.strftime('%d/%m/%Y'))
                x11 = dt.strftime('%d/%m/%Y')
                x12 = time.mktime(datetime.strptime(x11, "%d/%m/%Y").timetuple())
                y3 = str(int(x12))
                # convert it into ms
                # y3 = str(int(x12) * 1000)
                self.start_year = y3
                print("final start year is ", self.start_year)
                print("startstatus is in now december11:", startstatus)
            except Exception as e:
                print(e)
            # code on 17/02/20 end
        else:
            self.start_year = ''
            startstatus = 0
            print("startstatus is in now december01 :", startstatus)

    def set_end_year(self, response):
        global startstatus
        if startstatus == 0:
            self.end_year = ''
            print("i came hereeeee")
        else:
            y = response.xpath(config_Xpath.EMPLOYMENT_END_DATE_XPATH).extract_first()
            if y == None:
                self.end_year = 'Present'
            else:
                # print(y)
                # print("************END YEAR************s**********")
                # input()
                x = y
                print(" x is in end_year", x)
                # code on 17/02/20 start
                from dateutil.parser import parse
                try:
                    dt = parse(x)
                    print(dt)
                    print(dt.strftime('%d/%m/%Y'))
                    x11 = dt.strftime('%d/%m/%Y')
                    x12 = time.mktime(datetime.strptime(x11, "%d/%m/%Y").timetuple())
                    y3 = str(int(x12))
                    # self.end_year = y3
                    # y3 = str(int(x12) * 1000)
                    self.end_year = y3
                    print("final end year is ", self.end_year)
                    # code on 17/02/20 end
                except Exception as e:
                    print(e)
    def set_industry_type(self, response):
        self.industry_type = ''

    def set_company_size(self, response):
        self.company_size = ''

    def set_duration(self, response):
        global companygroupstatus
        print("companygroupstatus is :", companygroupstatus)
        if companygroupstatus == 0:
            x = response.xpath(
                config_Xpath.EMPLOYMENT_DURATION_XPATH).extract_first()
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
                            totalduration = int(t11[0])*12
                            ts = str(totalduration)
                            self.duration = ts
            else:
                self.duration = ''

            print("Final duration is", self.duration)
        if companygroupstatus == 1:
            groupdurationxpath = ".//*[@class='date-range__duration']/text()"
            self.duration = ''
            x = response.xpath(groupdurationxpath).extract_first()
            print("durn:", x)
            if x:
                t11 = x.split(' ')
                ts = ''
                if len(t11) == 4:
                    if t11[0].isdigit():
                        print("t11[0] is",t11[0])
                        durationint = int(t11[0]) * 12
                        if t11[2].isdigit():
                            print("t11[0] is", t11[2])
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
                self.duration = ''

    def set_designation(self, response):
        global companygroupstatus
        i = 0
        employeeprofilexpath = ".//*[contains(@class, 'result-card__title experience-group-position__title')]/text()"
        designation_rcvd = response.xpath(employeeprofilexpath).extract_first()
        if designation_rcvd is not None:
            companygroupstatus = 1
            print("companygroupstatus is :", companygroupstatus)
            print("designation_rcvd is :", designation_rcvd)
            d1 = designation_rcvd.encode(encoding='UTF-8', errors='strict').decode()
            d2 = html.unescape(str(d1))
            self.designation = d2.replace("'", "").replace('"', '')
            self.seniority_level = ''
            self.function = []
        if designation_rcvd is None:
            designation_typical = response.xpath(
                config_Xpath.EMPLOYEE_PROFILE_XPATH).extract()
            print("designation is:", designation_typical)
            if designation_typical:
                d3 = designation_typical[0].encode(encoding='UTF-8', errors='strict').decode()
                d4 = html.unescape(str(d3))
                self.designation = d4.replace("'", "").replace('"', '')
                self.seniority_level = ''
                self.function = []

    def set_is_latest(self, response):
        if self.get_end_year() == 'Present':
            self.is_latest = True
        else:
            self.is_latest = False

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

    def get_is_latest(self):
        return self.is_latest
