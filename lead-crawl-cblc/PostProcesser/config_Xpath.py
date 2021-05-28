LEAD_NAME_XPATH = "//*[@class='top-card-layout__title']/text()"
LOCATION_XPATH = "//*[@class='top-card__subline-item']/text()"
PROFILE_IMAGE = "//*[contains(@class, 'top-card-layout__entity-image')]/@src"
PROFILE_IMAGE_DELAYED = "//*[contains(@class, 'top-card-layout__entity-image')]/@data-delayed-url"
# PROFILE_DISP = "/html/head/meta[11]/@content"
#code added
PROFILE_DISP = "/html/head/meta[@property='og:image']/@content"
EDUCATION_LIST = "//*[@class='education__list']"
EDUCATION_BLOCK = ".//div[@class='result-card__contents']"
SCHOOL_XPATH = ".//*[@class='result-card__title']/text()"
DEGREE_INFO_XPATH = ".//*[@class='result-card__subtitle']/span"
EDUCATION_START_YEAR_XPATH = ".//*[contains(@class,'date-range__start-date')]/text()"
EDUCATION_END_YEAR_XPATH = ".//*[contains(@class,'date-range__end-date')]/text()"
EDUCATION_DEPARTMENT = ".//div[@class='result-card__contents']/h4[1]/span[2]"

#ANIKET
EXPERIENCE_BLOCK = "//*[contains(@class, 'experience-item__contents')]"
#2nd option ANSHUMAN
EXPERIENCE_ALT_BLOCK = ".//*[@class='experience-group experience-item']"

#main
# EXPERIENCE_NEW_BLOCK = "//*[contains(@class, 'result-card experience')]"

EMPLOYMENT_START_DATE_XPATH = ".//*[contains(@class, 'date-range__start-date')]/text()"
EMPLOYMENT_END_DATE_XPATH = ".//*[contains(@class, 'date-range__end-date')]/text()"
EMPLOYMENT_DURATION_XPATH = ".//*[contains(@class, 'date-range__duration')]/text()"
EMPLOYEE_PROFILE_XPATH = ".//*[contains(@class, 'experience-item__title')]/text()"
COMPANY_LOCATION_XPATH = ".//*[contains(@class, 'experience-item__location')]/text()"
#1st option
COMPANY_TITLE_XPATH = ".//*[contains(@class, 'experience-item__subtitle')]/a/text()"
#2nd option
#SHREYA,ANIKET
COMPANY_ALT_TITLE_XPATH = ".//*[@class='result-card__subtitle experience-item__subtitle']"
#3rd option
#ANSHUMAN
COMPANY_DIFF_TITLE_XPATH = ".//*[@class='experience-group experience-item']/a/@title"

COMPANY_URL_XPATH = ".//*[contains(@class, 'experience-item__subtitle')]/a/@href"

nameofcompanyxpath = ".//*[@class='experience-group-header__company']"

COMPANY_ABOUT_WEBSITE = "*//dt[contains(., 'Website')]/following-sibling::dd[1]/a/text()"
COMPANY_INDUSTRY_TYPE = "normalize-space(*//dt[contains(., 'Industries')]/following-sibling::dd[1]/text())"
COMPANY_SIZE = "normalize-space(*//dt[contains(., 'Company size')]/following-sibling::dd[1]/text())"

CSV_PATH = 'assets/Data Lake Mapping.csv'

LATEST_COMPANY_URL_XPATH = ".//*[contains(@class, 'top-card__right-column-link top-card__position-info with-transition')]/a/@href"
LATEST_COMPANY_NAME_XPATH = ".//div[@class='top-card-layout__entity-info']/h1[1]/text()"

pref = 'LOCAL_SOURCE_LINKEDIN_'
