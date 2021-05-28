import time
from datetime import datetime,timezone
from dateutil.parser import parse
import config_Xpath
from experience_details import ExperienceDetails
exp_details = ExperienceDetails()
import re
import html


FIVE_SCALE = 5.0
TEN_SCALE = 10.0
HUNDRED_SCALE = 100.0
FOUR_SCALE = 4.0



def getScale(score):
    scale = 0.0
    if (score > FIVE_SCALE and score <= TEN_SCALE):
        scale = TEN_SCALE
    elif score > 0 and score <= FOUR_SCALE:
        scale = FOUR_SCALE
    elif score > 4 and score <= FIVE_SCALE:
        scale = FIVE_SCALE
    elif score > TEN_SCALE:
        scale = HUNDRED_SCALE
    return scale

class EducationDetails:
    def __init__(self):
        start_year = ''
        end_year = ''
        college = ''
        department = ''
        degree = ''
        score = ''
        scale = ''
        is_latest = ''

    def set_education_details(self, response):
        self.set_scale(response)
        self.set_start_year(response)
        self.set_end_year(response)
        self.set_college(response)
        # self.set_department(response)
        self.set_degree_info(response)
        # self.set_score(response)
        self.set_is_latest(response)

    def set_start_year(self, response):
        startint = response.xpath(config_Xpath.EDUCATION_START_YEAR_XPATH).extract_first()
        if startint is None:
            self.start_year = ''
        else:
            # code on 17/02/20 start
            x = startint
            print(x)
            from dateutil.parser import parse
            dt = parse(x)
            # print(dt)
            # print(dt.strftime('%d/%m/%Y'))
            # x11 = dt.strftime('%d/%m/%Y')
            # x12 = time.mktime(datetime.strptime(x11, "%d/%m/%Y").timetuple())
            # y3 = str(int(x12))
            # convert it into ms
            # y3 = str(int(x12) * 1000)
            timestamp1 = str(int(dt.replace(tzinfo=timezone.utc).timestamp()) * 1000)
            self.start_year = timestamp1
            print("self.start_year",self.start_year)
            # input("enter")
            # code on 17/02/20 end

    def set_college(self, response):
        clgname = response.xpath(config_Xpath.SCHOOL_XPATH).extract_first()
        if clgname:
            #self.college = clgname.encode('ascii', 'ignore').decode()
            x = clgname.encode(encoding='UTF-8', errors='strict').decode()
            xx = html.unescape(str(x))
            self.college = xx.replace("'", "").replace('"', '')

    def set_end_year(self, response):
        endint = response.xpath(config_Xpath.EDUCATION_END_YEAR_XPATH).extract_first()
        if endint is None:
            self.end_year = ''
        else:
            # code on 17/02/20 start
            x = endint
            print(x)
            from dateutil.parser import parse
            dt = parse(x)
            # print(dt)
            # print(dt.strftime('%d/%m/%Y'))
            # x11 = dt.strftime('%d/%m/%Y')
            # x12 = time.mktime(datetime.strptime(x11, "%d/%m/%Y").timetuple())
            # y3 = str(int(x12))
            # self.end_year = y3
            # convert it into ms
            # y3 = str(int(x12) * 1000)
            timestamp2 = str(int(dt.replace(tzinfo=timezone.utc).timestamp()) * 1000)
            self.end_year = timestamp2
            print("self.end_year",self.end_year)
            # input("enter")
            # code on 17/02/20 end


    def set_degree_info(self, response):
        degree_info = response.xpath(config_Xpath.DEGREE_INFO_XPATH).extract()
        if degree_info:
            if len(degree_info) == 3:
                degreeint = degree_info[0]
                t = degreeint.split('>')
                t1 = t[1].split('<')
                t2 = t1[0]
                d1 = t2.encode(encoding='UTF-8', errors='strict').decode()
                d1x = html.unescape(str(d1))
                self.degree = d1x.replace("'", "").replace('"', '')
                degre_value = self.degree
                try:
                    r1 = re.match(r'[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?', degre_value)
                    print(r1.group())
                    value = r1.group()
                    self.department = ''
                    self.degree = ''
                    self.score = str(value)
                    cgpa = float(self.score)
                    scaleint = getScale(cgpa)
                    self.scale = str(scaleint)
                except:
                    value = ''
                    self.score = ''
                    self.scale = ''
                    print("value is :", value)
                    self.degree = degre_value
                    self.department = ''
                deptint = degree_info[1]
                x = deptint.split('>')
                x1 = x[1].split('<')
                x2 = x1[0]
                d2 = x2.encode(encoding='UTF-8', errors='strict').decode()
                d2x = html.unescape(str(d2))
                self.department = d2x.replace("'", "").replace('"', '')
                x = self.department
                self.score = ''
                self.scale = ''
                try:
                    r1 = re.match(r'[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?', x)
                    print(r1.group())
                    value = r1.group()
                    self.department = ''
                    self.score = str(value)
                    cgpa = float(self.score)
                    scaleint = getScale(cgpa)
                    self.scale = str(scaleint)
                except:
                    value = ''
                    self.score = ''
                    self.scale = ''
                    print("value is :", value)
                    self.department = x
                scoreint = degree_info[2]
                y = scoreint.split('>')
                y1 = y[1].split('<')
                y2 = y1[0]
                d3 = y2.encode(encoding='UTF-8', errors='strict').decode()
                d3x = html.unescape(str(d3))
                self.score = d3x.replace("'", "").replace('"', '')
                xx = self.score
                try:
                    r1 = re.match(r'[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?', xx)
                    print(r1.group())
                    value = r1.group()
                    self.score = str(value)
                    cgpa = float(self.score)
                    scaleint = getScale(cgpa)
                    self.scale = str(scaleint)
                except:
                    value = ''
                    print("value is :", value)
                    self.score = ''
                    self.scale = ''

            elif len(degree_info) == 2:
                degreeint = degree_info[0]
                t = degreeint.split('>')
                t1 = t[1].split('<')
                t2 = t1[0]
                d4 = t2.encode(encoding='UTF-8', errors='strict').decode()
                d4x = html.unescape(str(d4))
                self.degree = d4x.replace("'", "").replace('"', '')
                degre_value = self.degree
                try:
                    r1 = re.match(r'[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?', degre_value)
                    print(r1.group())
                    value = r1.group()
                    self.department = ''
                    self.degree = ''
                    self.score = str(value)
                    cgpa = float(self.score)
                    scaleint = getScale(cgpa)
                    self.scale = str(scaleint)
                except:
                    value = ''
                    self.score = ''
                    self.scale = ''
                    print("value is :", value)
                    self.degree = degre_value
                    self.department = ''
                deptint = degree_info[1]
                x = deptint.split('>')
                x1 = x[1].split('<')
                x2 = x1[0]
                d5 = x2.encode(encoding='UTF-8', errors='strict').decode()
                d5x = html.unescape(str(d5))
                self.department = d5x.replace("'", "").replace('"', '')
                self.score = ''
                self.scale = ''
                x = self.department
                try:
                    r1 = re.match(r'[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?', x)
                    print(r1.group())
                    value = r1.group()
                    self.score = str(value)
                    cgpa = float(self.score)
                    scaleint = getScale(cgpa)
                    self.scale = str(scaleint)
                    self.department = ''
                except:
                    value = ''
                    print("value is :", value)
                    self.score = ''
                    self.department = x
                    self.scale = ''
            else:
                degreeint = degree_info[0]
                t = degreeint.split('>')
                t1 = t[1].split('<')
                t2 = t1[0]

                try:
                    r1 = re.match(r'[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?', t2)
                    print(r1.group())
                    value = r1.group()
                    self.score = str(value)
                    cgpa = float(self.score)
                    scaleint = getScale(cgpa)
                    self.scale = str(scaleint)
                    self.department = ''
                    self.degree = ''
                except:
                    value = ''
                    print("value is :", value)
                    d6 = t2.encode(encoding='UTF-8', errors='strict').decode()
                    d6x = html.unescape(str(d6))
                    self.degree = d6x.replace("'", "").replace('"', '')
                    self.department = ''
                    self.scale = ''
                    self.score = ''
        else:
            self.degree = ''
            self.department = ''
            self.score = ''
            self.scale = ''

    def set_scale(self, response):
        self.scale = ''

    def set_is_latest(self, response):
        if exp_details.company is None:
            self.is_latest = True
        else:
            self.is_latest = False



    def get_college(self):
        return self.college

    def get_start_year(self):
        return self.start_year

    def get_end_year(self):
        return self.end_year

    def get_degree(self):
        return self.degree

    def get_department(self):
        return self.department

    def get_score(self):
        return self.score

    def get_scale(self):
        return self.scale

    def get_is_latest(self, response):
        return self.is_latest
