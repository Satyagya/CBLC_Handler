package com.example.LeadCrawl.services.implementation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.LeadCrawl.config.S3Config;
import com.example.LeadCrawl.constants.Constants;
import com.example.LeadCrawl.services.EmailGenerationService;
import com.example.LeadCrawl.services.LeadCrawlService;
import com.example.LeadCrawl.services.helpers.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import static com.example.LeadCrawl.constants.Constants.*;
import static com.example.LeadCrawl.constants.Constants.ALL_SEARCH_ENGINES_IN_SLEEP;
import static com.example.LeadCrawl.model.Attribute.FIRST_NAME;
import static com.example.LeadCrawl.model.Attribute.LAST_NAME;
import static com.example.LeadCrawl.model.Attribute.PROFILE_URL;
import static com.example.LeadCrawl.model.Attribute.URL_HASH_CODE;


@Service("com.example.LeadCrawl.services.implementation.LeadCrawlServiceImplementation")
@Slf4j
public class LeadCrawlServiceImplementation implements LeadCrawlService {

  @Autowired
  private S3Config s3Config;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Notifier")
  private Notifier notifier;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Drivers")
  private Drivers drivers;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Helper")
  private Helper helper;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.MailManager")
  private MailManager mailManager;

  @Autowired
  private CreatePayload createPayload;

  @Autowired
  private MariaDbConnector mariaDbConnector;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.CloudCrawler")
  private CloudCrawler cloudCrawler;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.CountryMatch")
  private CountryMatch countryMatch;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.implementation.EmailGenerationServiceImpl")
  private EmailGenerationService emailGenerationService;

  @Value("${aws.bucket.name}")
  private String bucketName;

  @Value("${aws.bucket.name.complete}")
  private String completdBucketName;

  @Value("${aws.bucket.name.input}")
  private String bucketNameInput;

  @Value("${aws.bucket.name.process}")
  private String bucketNameProcess;

  @Value("${aws.bucket.name.output}")
  private String bucketNameOutput;

  @Value("${output.file.prefix}")
  private String ouputFilePrefix;

  @Value("${input.file.prefix}")
  private String inputFilePrefix;

  @Value("${json.array.profileurl.size}")
  private int jsonArrayProfileSize;

  @Value("${input.path}")
  private String inputDirectory;

  @Value("${output.path}")
  private String outputDirectory;

  @Value("${column.company.name}")
  private String columnCompanyName;

  @Value("${column.company.website}")
  private String columnWebsite;

  @Value("${download.base.url}")
  private String baseUrl;

  @Value("${result.limit.for.companies}")
  private int resultLimitForCompanies;

  @Value("${acronym.length.limit}")
  private int acronymLength;

  @Value("${program.sleep.time}")
  private int programSleepTime;

  //Global to file
  private int noDataCount;
  private int dataButCompanyNotMatched;
  private int dataButJustCompanyMatched;
  private int dataWithCompanyAndResultMatched;

  private int companySpecific_noDataCount;
  private int companySpecific_dataButCompanyNotMatched;
  private int companySpecific_dataButJustCompanyMatched;
  private int companySpecific_dataWithCompanyAndResultMatched;

  private int resultFoundForCompanyCounter;
  private List<Map<String, String>> countryCodeMapList;
  private Random random=new Random();

  private RedissonClient redissonClient;

  @PostConstruct
  private void init() {
    redissonClient = Redisson.create();
    resultFoundForCompanyCounter=0;
    countryCodeMapList = countryMatch.getCountryCode();
  }

  //@Scheduled(fixedDelayString = "${poll.frequency.millis}")
  public void getLatestFileFromS3() {
    String inputPath = null;
    String outputPath = null;

    //TOASK====================================================================
    HashMap<String, String> desgMap = mariaDbConnector.connectDb(MARIADB_DESG_COLUMN);
    HashMap<String, String> mailMap = mariaDbConnector.connectDb(MARIADB_MAIL_COLUMN);

    AmazonS3 s3Client;
    String latestFile = null;
    String FolderRegex = "(LEAD_CRAWL_CSV/TEST_INPUT/)";
    s3Client = s3Config.getS3Client();
    List<String> objectKey = new ArrayList<>();
    RLock rLock = getRLock();
    if (rLock != null)
    {
      try
      {
        ListObjectsRequest listObjectsRequest =
            new ListObjectsRequest().withBucketName(bucketName).withPrefix("LEAD_CRAWL_CSV/TEST_INPUT");
        ObjectListing objectListing;
        do
        {
          objectListing = s3Client.listObjects(listObjectsRequest);
          for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
          {
            //objectSummary.
            log.info("Files in Input bucket "+objectSummary.getKey());
            objectKey.add(objectSummary.getKey());//
          }
          listObjectsRequest.setMarker(objectListing.getNextMarker());
        }

        //TOASK====================================================================
        while (objectListing.isTruncated());

        if (CollectionUtils.isNotEmpty(objectKey) && objectKey.size() > 1)
        {
          for (String anObjectKey : objectKey)
          {
            if (StringUtils.isNotEmpty(anObjectKey.replaceFirst(FolderRegex, "")))
            {
              latestFile = anObjectKey.replaceFirst(FolderRegex, "");
              break;
            }
          }
        }

        if (StringUtils.isNotEmpty(latestFile))
        {
          log.info("Downloading the input file {} from s3 bucket for lead crawling", latestFile);
          String outPutFile = getOutPutFileName(latestFile);
          inputPath = inputDirectory + latestFile;
          outputPath = outputDirectory + outPutFile;

          File localFile = new File(inputPath);
          try
          {
            ObjectMetadata object =
                s3Client.getObject(new GetObjectRequest(bucketNameInput, latestFile), localFile);
            System.out.println(object);
            CopyObjectResult copyObjectResult =
                s3Client.copyObject(bucketNameInput, latestFile, bucketNameProcess, latestFile);
            s3Client.deleteObject(bucketNameInput, latestFile);
          }
          catch (Exception e)
          {
            log.error("Exception occurred while downloading the input file from s3 bucket", e);
          }
        }
        else
        {
          log.info("No file available to crawl at S3 Bucket");
        }
      }
      catch (Exception e) {
        String errorMessage = "Error occured while taking the input file from S3 bucket "+e;
        notifier.notifySlack(errorMessage);
        log.error(errorMessage + " Failed due to: {}", e);
      }
      finally
      {
        rLock.unlock();
      }
      if (StringUtils.isNotEmpty(latestFile))
      {
        String prod = getProdName(latestFile);
        String request_id = latestFile.split("_")[2];
        LocalTime time = LocalTime.now();
        String message =
            latestFile + " File downloaded from S3 bucket for Lead Crawling at " + time.toString();
        notifier.notifySlack(message);
        startCrawlingForSearchProfiles(inputPath, outputPath, latestFile, request_id, prod, desgMap,
            mailMap);
      }
      else
      {
        log.info("No file Available to crawl");
      }
    }
    else
    {
      log.info("Unable to access s3 due to lock not being acquired");
    }
  }

  private String getOutPutFileName(String inputFile) {
    String prefix = inputFile.split("\\.")[0];
    String outputPrefix = prefix + "_output";
    return inputFile.replace(prefix, outputPrefix);
  }

  private RLock getRLock() {
    RLock rLock = null;
    try {
      rLock = redissonClient.getLock("GLOBAL_LOCK");
      rLock.lock();
    } catch (Exception e) {
      log.error("Exception while gaining Lock {}", e);
    }
    return rLock;
  }
  private void initializeCounterValues(){
    noDataCount=0;
    dataButCompanyNotMatched=0;
    dataButJustCompanyMatched=0;
    dataWithCompanyAndResultMatched=0;
  }
  private void initializeCompanySpecificCounterValues(){
    companySpecific_noDataCount = 0;
    companySpecific_dataButCompanyNotMatched = 0;
    companySpecific_dataButJustCompanyMatched = 0;
    companySpecific_dataWithCompanyAndResultMatched = 0;
  }
  private void incrementCounter(int match){
    switch (match){
      case 1:
        noDataCount++;
        break;

      case 2:
        dataButCompanyNotMatched++;
        break;

      case 3:
        dataButJustCompanyMatched++;
        break;

      case 4:
        dataWithCompanyAndResultMatched++;
        break;

      default:
        log.error("Wrong value provided for increment count case.");
    }
  }
  private void incrementCompanySpecificCounter(int match){
    switch (match){
      case 1:
        companySpecific_noDataCount++;
        break;

      case 2:
        companySpecific_dataButCompanyNotMatched++;
        break;

      case 3:
        companySpecific_dataButJustCompanyMatched++;
        break;

      case 4:
        companySpecific_dataWithCompanyAndResultMatched++;
        break;

      default:
        log.error("Wrong value provided for increment count case.");
    }
  }

   private String getDesignationInUrl(String[] splittedSearchTitle){
    int length = splittedSearchTitle.length;
    String designationWithJobFunction = "";
    for (int i=0;i<length;i++){
      if (i!=0 && i!=length-1)
        designationWithJobFunction += splittedSearchTitle[i]+Constants.DASH;
    }
    designationWithJobFunction = designationWithJobFunction.substring(0, designationWithJobFunction.length()-1).trim();
    return designationWithJobFunction;
  }

  private List<String[]> checkDuplicates(String pathForFile) {
    List<String[]> arrayLists = new ArrayList<>();
    List<String> websiteList = new ArrayList<>();
    try {
      FileReader fileReader = new FileReader(pathForFile);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] nextRecord;
      int count = 0;
      while ((nextRecord = csvReader.readNext()) != null) {
        if (count == 0) {
          arrayLists.add(nextRecord);
          count++;
          continue;
        }
        if (!websiteList.contains(nextRecord[2])) {
          websiteList.add(nextRecord[2]);
          arrayLists.add(nextRecord);
        }
        count++;
      }
    } catch (IOException e) {
      log.error("Error occurred while checking duplicates: "+e.toString());
      notifier.notifySlack("Error occurred while checking duplicates: "+e.toString());
    }
    return arrayLists;
  }

  private String removeUnwantedCharacters(String name){
    if (name.contains("|"))
      name = name.replace("|","");
    return name;
  }

  private String getCountryCode(String country){
    String code=null;
    for (Map<String, String> map : countryCodeMapList){
      if(map.containsKey(country)){
        code = map.get(country);
      }
    }
    return code;
  }


  private String startCrawlingForSearchProfiles(String pathForFile, String pathToSave,
      String latestFile, String request_id, String prodName, HashMap<String, String> desgMap,
      HashMap<String, String> mailMap)
  {
    List<JSONObject> leadList = null;
    int totalSearches = 0;
    int designationResultsFound=0;
    int designationResultsNotFound=0;
    initializeCounterValues();
    try
    {
      List<String[]> arrayLists = checkDuplicates(pathForFile);

      String[] designationList = desgMap.get(prodName).split(",");
      log.info("Lead crawling started for the file {} for the designations {}", latestFile,
          Arrays.toString(designationList));
      String message =
          "Lead crawling started for the file " + latestFile + " at " + LocalTime.now().toString();
      notifier.notifySlack(message);
      int count = 0;
      try
      {
        FileWriter statsFile = new FileWriter(outputDirectory+"stats.txt");
        FileWriter outputfile = new FileWriter(pathToSave);
        CSVWriter writer = new CSVWriter(outputfile);
        String[] header = {"NAME", "WEBSITE", "PROFILE_URL", "FIRST_NAME", "LAST_NAME", "MATCH_DESIGNATION", "EMAIL_ID"};
        writer.writeNext(header);

        for (String[] nextRecord : arrayLists)
        {
          resultFoundForCompanyCounter=0;
          initializeCompanySpecificCounterValues();
          designationResultsFound=0;
          designationResultsNotFound=0;

          if (count == 0) {
            count++;
            continue;
          }
          // adding 5-10s of sleep time for each company
          TimeUnit.SECONDS.sleep(5+random.nextInt(5));
          String code = getCountryCode(nextRecord[7]);

          for (String designation : designationList)
          {

            List<String> dataArray = new ArrayList<>();
            String name = nextRecord[0];
            name = removeUnwantedCharacters(name);
            dataArray.add(name);
            String website = nextRecord[2];
            String country = nextRecord[7];
            dataArray.add(country);

            if (designation.contains(":"))
            {
              String designationOriginal = designation.split(":")[0];
              String designationAcronym = designation.split(":")[1];

              leadList = getLeadList(dataArray, designationAcronym, name, code);
              if(leadList.size()>0)
                writeDetailsInCSV(leadList, name, website, designationAcronym, writer);
              else
              {
                dataArray.clear();
                dataArray.add(name);
                dataArray.add(country);
                leadList = getLeadList(dataArray, designationOriginal, name, code);
                writeDetailsInCSV(leadList, name, website, designationOriginal, writer);
              }
            }
            else
            {
              leadList = getLeadList(dataArray, designation, name, code);
              writeDetailsInCSV(leadList, name, website, designation, writer);
            }
            // counting whether result found or not
            if (leadList.size()==0)
              designationResultsNotFound++;
            else
              designationResultsFound++;

            //adding 12-22s wait time for each search query
            TimeUnit.SECONDS.sleep(12+random.nextInt(10));

            //for ending here
            totalSearches++;
          }

          //writing for statsfile
          statsFile.append("for company:"+nextRecord[0]+"\n");
          statsFile.append("Number of searches for which no data was found: "+String.valueOf(companySpecific_noDataCount)+"\n");
          statsFile.append("Number of searches for which data was found but Company not matched: "+ String.valueOf(companySpecific_dataButCompanyNotMatched)+"\n");
          statsFile.append("Number of searches for which data was found and only Company matched: "+ String.valueOf(companySpecific_dataButJustCompanyMatched)+"\n");
          statsFile.append("Number of searches for which data was found and Company & designation both matched: "+ String.valueOf(companySpecific_dataWithCompanyAndResultMatched)+"\n");
          statsFile.append("\n");

          log.info("For company:{}, number of designations for result found {}",nextRecord[0],String.valueOf(designationResultsFound));
          log.info("For company:{}, Number of designations for result not found {}",nextRecord[0],String.valueOf(designationResultsNotFound));

          if (count % 5 == 0)
          {
            writer.flush();
            statsFile.flush();
          }
          count++;
          //TODO: write this in log
          System.out.println("Company Count: "+String.valueOf(count));
        }

        log.info("total queries searched: {}",String.valueOf(totalSearches));
        log.info("Number of searches for which no data was found: {}", String.valueOf(noDataCount));
        log.info("Number of searches for which data was found but Company not matched: {}", String.valueOf(dataButCompanyNotMatched));
        log.info("Number of searches for which data was found and only Company matched: {}", String.valueOf(dataButJustCompanyMatched));
        log.info("Number of searches for which data was found and Company & designation both matched: {}", String.valueOf(dataWithCompanyAndResultMatched));


        statsFile.close();
        writer.close();
      }
      catch (Exception e)
      {
        String errorMessage = "Error occurred while crawling leads data for "+latestFile+" due to "+e;
        notifier.notifySlack(errorMessage);
        log.error(e.toString());
      }
    }
    catch (Exception e) {
      String errorMessage = "Error occurred while crawling leads data for "+latestFile+" due to "+e;
      notifier.notifySlack(errorMessage);
      log.error(e.toString());
    }

    pathToSave = checkDuplicateLeadsData(pathToSave);
    File file = new File(pathToSave);
    log.info("Lead Crawling completed for the file {}", latestFile);
    String message =
        "Lead Crawling Completed for the file " + latestFile + " at " + LocalTime.now().toString();
    notifier.notifySlack(message);
    try
    {
      log.info("Uploading the leads file {} into s3 bucket", file.getName());
      AmazonS3 s3 = s3Config.getS3Client();
      String indexKey = ouputFilePrefix + request_id + ".csv";
      s3.putObject(bucketNameOutput, file.getName(), file);
      CopyObjectResult copyObjectResult =
          s3.copyObject(bucketNameProcess, latestFile, completdBucketName, latestFile);
      s3.deleteObject(bucketNameProcess, latestFile);
    }
    catch (Exception e) {
      String errorMessage = "Error occurred while uploading the leads data into s3 bucket due to "+e;
      notifier.notifySlack(errorMessage);
      log.error("Error while uploading the leads file in s3 bucket", e);
    }
    String[] mailsList = mailMap.get(prodName).split(",");
    String leadMailMessage =
        "Email for Leads Data has been sent to respective email accounts for the product "
            + prodName + " with download link " + "\n" + baseUrl + file.getName();
    for (String aMailsList : mailsList) {
      log.info("uploading the leads file {} to {} ", file.getName(), aMailsList);
      String subject = "Leads Data for file" + latestFile;
      mailManager.sendMail(pathToSave, aMailsList, subject, prodName, "LEADS");
    }

    notifier.notifySlack(leadMailMessage);

    log.info("Email generation Process started for the file {}", latestFile);
    String messageForEmailSending =
        "Email generation Process started for the file " + latestFile + " at " + LocalTime.now()
            .toString();
    notifier.notifySlack(messageForEmailSending);

    String csvPathForFinalCsv = emailGenerationService.generateEmailForCsv(pathToSave);

    if (StringUtils.isNotEmpty(csvPathForFinalCsv)) {
      File mailFile = new File(csvPathForFinalCsv);

      String MailMessage = "Email for " + mailFile.getName()
          + " has been sent to respective email accounts for the product " + prodName
          + " with download link " + "\n" + baseUrl + mailFile.getName();

      log.info("Email generation Process completed for the file {}", latestFile);
      String messageForEmailCompleted =
          "Email generation Process completed for the file " + latestFile + " at " + LocalTime.now()
              .toString();
      notifier.notifySlack(messageForEmailCompleted);
      for (String aMailsList : mailsList) {
        log.info("uploading the mails file to {} ", aMailsList);
        String subject = "Email-ids Data for file" + latestFile;
        mailManager.sendMail(csvPathForFinalCsv, aMailsList, subject, prodName, "MAIL");
      }
      notifier.notifySlack(MailMessage);
    }

    createPayload.createPayload(pathToSave);
    return "success";
  }

  private List<JSONObject> getLeadList(List<String> dataArray, String designation, String name, String code) {
    List<JSONObject> leadList;
    boolean foundResult = false;
    dataArray.add(designation);
    try {
      String urlToSearch = getPersonaSearchUrl(dataArray);
      JSONObject crawledData = cloudCrawler.getCrawledData(urlToSearch);
      //TOASK ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
      boolean ifAllsearchEnginesInSleep = (boolean) crawledData.get(ALL_SEARCH_ENGINES_IN_SLEEP);

      if (ifAllsearchEnginesInSleep){
        log.info("All Search Engines in sleep. Program going to sleep for upto {} seconds", (60 + programSleepTime));
        makeProgramSleep();
      }
      else
      {
        //trying to get result with 2 other search results if no data was found
        for (int i = 0; i < 2; i++) {
          TimeUnit.SECONDS.sleep(10+random.nextInt(6));
          if (checkIfResultPresent(crawledData))
          {
            foundResult = true;
            break;
          } else
          {
            log.error("NODATA for query: {}", urlToSearch);
            crawledData = cloudCrawler.getCrawledData(urlToSearch);
          }
        }
      }

//TOASK ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
      if (!foundResult) {
        log.error("NODATA for query: {}, after trying 3 times", urlToSearch);
      }
      else {
        log.info("Url to Search: {}", urlToSearch);
        log.info("Data crawled: {}", crawledData.toJSONString());
      }

      Set<JSONObject> leadSet = new HashSet<>();
      if (code==null)
        code = "";

      //TOASK ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
      leadSet.addAll(extractProfileUrl(designation, name, code, urlToSearch, crawledData));
      leadList = new ArrayList<>(leadSet);
    }
    catch (Exception e){
      e.printStackTrace();
      leadList = new ArrayList<>();
      log.error("Error occurred while getting crawled Data");
    }
    return leadList;
  }

  private JSONArray extractProfileUrl(String designation, String companyName, String countryCode,
                                      String currentUrl, JSONObject crawledData) {
    JSONArray jsonArray = new JSONArray();
    companyName = companyName + " | LinkedIn";
    String countryCodeMatchInUrl = countryCode + ".linkedin.com";
    int count = 0;
    boolean companyMatch = false;
    if (null!=crawledData){
        JSONArray resultList = (JSONArray)crawledData.get("results");
        try {
          if (CollectionUtils.isNotEmpty(resultList)) {
            for (Object element : resultList) {

              if(resultFoundForCompanyCounter == resultLimitForCompanies)
                break;

              JSONObject jsonObject = (JSONObject) element;
              String searchTitle = (String) jsonObject.get("title");
//              searchTitle = searchTitle.toLowerCase();
              String[] checkLength = searchTitle.split("-");
              String designationInUrl = null;
              String companyInUrl = null;
              if (checkLength.length > 2) {
                designationInUrl = getDesignationInUrl(checkLength);

                if (!(designation.length() < acronymLength)){
                  designation = designation.toLowerCase();
                  designationInUrl = designationInUrl.toLowerCase();
                }

                companyInUrl = checkLength[checkLength.length - 1].trim();
                companyInUrl = StringUtils.stripAccents(companyInUrl);
                companyName = StringUtils.stripAccents(companyName);
                if (companyInUrl.equalsIgnoreCase(companyName))
                  companyMatch = true;
                if (designationInUrl.contains(designation) && companyInUrl.equalsIgnoreCase(companyName)) {
                  resultFoundForCompanyCounter++;
                  String profileUrl = (String) jsonObject.get("link");
                  if (profileUrl.contains(countryCodeMatchInUrl)) {
                    profileUrl = helper.decodeUrl(profileUrl);
                    String urlHashCode = helper.getHashCode(profileUrl);
                    JSONObject savedJson = saveExtractedValues(profileUrl, urlHashCode, searchTitle);
                    jsonArray.add(savedJson);
                  }
                }
              }
            }
            if (jsonArray.size() <= jsonArrayProfileSize && resultFoundForCompanyCounter < resultLimitForCompanies) {
              for (count = 0; count < resultList.size(); count++) {

                if(resultFoundForCompanyCounter == resultLimitForCompanies)
                  break;

                Object element = resultList.get(count);
                JSONObject jsonObject = (JSONObject) element;
                String searchTitle = (String) jsonObject.get("title");
//                searchTitle = searchTitle.toLowerCase();
                String[] checkLength = searchTitle.split("-");
                String designationInUrl = null;
                String companyInUrl = null;
                if (checkLength.length > 2) {
                  designationInUrl = getDesignationInUrl(checkLength);

                  if (!(designation.length() < acronymLength)){
                    designation = designation.toLowerCase();
                    designationInUrl = designationInUrl.toLowerCase();
                  }
                  //do partial match with company/matching firstname
                  companyInUrl = checkLength[checkLength.length - 1].trim().split(" ")[0];
                  companyInUrl = StringUtils.stripAccents(companyInUrl);
                  companyName = StringUtils.stripAccents(companyName);
                  if (companyInUrl.contains("..."))
                    companyInUrl = companyInUrl.replace("...","").trim();
                  companyName = companyName.split(" ")[0];
                  if (companyInUrl.equalsIgnoreCase(companyName) && designationInUrl.contains(designation)) {
                    resultFoundForCompanyCounter++;
                    String profileUrl = (String) jsonObject.get("link");
                    if (profileUrl.contains(countryCodeMatchInUrl)) {
                      profileUrl = helper.decodeUrl(profileUrl);
                      String urlHashCode = helper.getHashCode(profileUrl);
                      JSONObject savedJson = saveExtractedValues(profileUrl, urlHashCode, searchTitle);
                      jsonArray.add(savedJson);
                    }
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          log.error("Error occurred while loading persona urls for navigationUrl: {}", currentUrl);
          notifier.notifySlack("Error occurred while loading persona urls for navigationUrl: " + currentUrl);
        }
    }
    if (companyMatch){
      if (jsonArray.size()!=0) {
        incrementCompanySpecificCounter(4);
        incrementCounter(4);
      }
      else {
        incrementCounter(3);
        incrementCompanySpecificCounter(3);
      }
    }
    else {
      incrementCounter(2);
      incrementCompanySpecificCounter(2);
    }
    return jsonArray;
  }

  private boolean checkIfResultPresent(JSONObject crawledData) {
    boolean leadPresent = false;
    if (null != crawledData) {
      JSONArray resultList = (JSONArray) crawledData.get("results");
      if (resultList.size() != 0) {
        leadPresent = true;
      }
    }
    return leadPresent;
  }


   String getPersonaSearchUrl(List<String> arrayList) {
    List<String> filterList = new ArrayList<>(arrayList);
    StringJoiner joiner = new StringJoiner(EMPTY_STRING, BING_QUERY_PREFIX, "full profile");
    filterList.stream().filter(StringUtils::isNotBlank)
            .map(filter -> String.format("%s%s", filter, Constants.AND)).forEachOrdered(joiner::add);
    return joiner.toString().replace(" AND full profile","");
  }

  private void makeProgramSleep(){
    try {
      TimeUnit.SECONDS.sleep(1200 + random.nextInt(programSleepTime));
    }
    catch (Exception e){
      log.error("Error while making program sleep. Reason: {}",e.toString());
    }
  }

  private void writeDetailsInCSV(List<JSONObject> leadList, String name, String website,String designation, CSVWriter writer){
    for (JSONObject jsonObject : leadList) {
      log.info("Got result for Company: {}, designation: {} -> {}", name, designation, jsonObject.toJSONString());
      String[] row = {name, website, (String) jsonObject.get("PROFILE_URL"),
              (String) jsonObject.get("FIRST_NAME"), (String) jsonObject.get("LAST_NAME"), designation};
      writer.writeNext(row);
    }
  }

  public JSONObject saveExtractedValues(String profileUrl, String urlHashCode, String text) {
//    log.info("Saving details for sourceName: LinkedInPersona - for profileUrl: {} urlHashCode: {}", profileUrl, urlHashCode);
    String[] firstAndLastNameArray = getName(text);
    JSONObject data = new JSONObject();
    data.put(URL_HASH_CODE.name(), urlHashCode);
    data.put(PROFILE_URL.name(), profileUrl);
    data.put(FIRST_NAME.name(), firstAndLastNameArray[0]);
    data.put(LAST_NAME.name(), firstAndLastNameArray[1]);
    return data;
  }

  private String[] getName(String text) {
    String[] firstAndLastNameArray = new String[2];
    String[] split = text.split("-");
    String name = split[0].replaceAll("([^0-9a-zA-Z\\s\\.]).*", "").trim();
    String[] nameArray = name.split(" ");
    if (nameArray.length == 1) {
      firstAndLastNameArray[0] = nameArray[0];
      firstAndLastNameArray[1] = "";
    } else if (nameArray.length == 2) {
      firstAndLastNameArray[0] = nameArray[0];
      firstAndLastNameArray[1] = nameArray[1];
    } else {
      firstAndLastNameArray[0] = nameArray[0];
      firstAndLastNameArray[1] = nameArray[nameArray.length - 1];
    }
    return firstAndLastNameArray;
  }

  private String getProdName(String fileName) {
    String prodName;
    prodName = fileName.split("_")[1];
    return prodName;
  }

  private String checkDuplicateLeadsData(String csvPath){
    List<String[]> arrayLists = new ArrayList<>();
    List<String> profileUrl = new ArrayList<>();
    try {
      FileReader fileReader = new FileReader(csvPath);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] nextRecord;
      int count=0;
      while ((nextRecord = csvReader.readNext()) != null){
        if (count==0){
          count++;
          continue;
        }
        arrayLists.add(nextRecord);
      }
      FileWriter fileWriter = new FileWriter(csvPath);
      CSVWriter csvWriter = new CSVWriter(fileWriter);
      String[] header = {"NAME", "WEBSITE", "PROFILE_URL", "FIRST_NAME", "LAST_NAME", "MATCH_DESIGNATION", "EMAIL_ID"};
      csvWriter.writeNext(header);
      for (String[] array:arrayLists){
        if (!profileUrl.contains(array[2])){
          profileUrl.add(array[2]);
          csvWriter.writeNext(array);
        }
      }
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return csvPath;
  }
}
