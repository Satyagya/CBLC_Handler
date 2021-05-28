import config_Xpath
from latest_company_details import LatestCompanyDetails

#recent code
from experience_details import ExperienceDetails


#new code
# from exp1 import ExperienceDetails
from exp1 import company
companyList = []




# from personal_details import PersonalDetails
from education_details import EducationDetails
from contact_details import ContactDetails
class ExtractData:
    def __init__(self):
        # personal_details = None
        education_details = None
        work_details = None


    def set_extract_data(self, response):
        # self.set_personal_details(response)
        self.set_education_details(response)
        self.set_work_details(response)


    #last-try
    def set_work_details(self, response):
        # EXPERIENCE_BLOCK IS FOR ANIKET-SINGLE
        total_experience = response.xpath(config_Xpath.EXPERIENCE_BLOCK)
        print("single experience is :",total_experience)
        print("length of single experience is :", len(total_experience))
        experience_list = []
        for experience in total_experience:
            exp = ExperienceDetails()
            exp.set_experience_details(experience)
            experience_list.append(exp.__dict__)
        newexpblock = "//*[@class='experience-group experience-item']"
        total_experience_prev = response.xpath(newexpblock)
        print("total_experience_prev is :",total_experience_prev)
        for exp_grp in total_experience_prev:
            print("exp grp is :",exp_grp)
            exp_grp_company_name = exp_grp.xpath(".//*[@class='experience-group-header__company']/text()").extract_first()
            try:
                print(type(exp_grp_company_name))
                print(exp_grp_company_name)
            except:
                print("no value")

            print("company name is :",exp_grp_company_name)
            x2 = exp_grp_company_name
            listgrpexpblock = ".//*[@class = 'result-card__contents experience-group-position__contents']"
            total_experience = exp_grp.xpath(listgrpexpblock)
            print("total_experience is : ",total_experience)
            # input("checkkkkk")
            for experience in total_experience:
                exp = ExperienceDetails()
                exp.set_experience_details(experience)
                experience_list.append(exp.__dict__)
                exp.__dict__['company'] = x2
        self.work_details = experience_list


    def set_education_details(self, response):
        total_education = response.xpath(config_Xpath.EDUCATION_LIST)
        education_list = []
        for education in total_education.xpath(config_Xpath.EDUCATION_BLOCK):
            ed = EducationDetails()
            ed.set_education_details(education)
            education_list.append(ed.__dict__)
        self.education_details = education_list


    # def set_personal_details(self, response):
    #     pd = PersonalDetails()
    #     pd.set_personal_details(response)
    #     self.personal_details = pd.__dict__


    def get_education_details(self):
        return self.education_details


    def get_work_details(self):
        return self.work_details


    # def get_personal_details(self):
    #     return self.personal_details
