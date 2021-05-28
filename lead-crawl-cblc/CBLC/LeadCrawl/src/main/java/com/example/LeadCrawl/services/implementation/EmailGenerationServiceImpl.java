package com.example.LeadCrawl.services.implementation;

import com.example.LeadCrawl.entity.EmailAccountInformation;
import com.example.LeadCrawl.model.ProfileDetails;
import com.example.LeadCrawl.repository.EmailAccountInformationRepository;
import com.example.LeadCrawl.services.EmailGenerationService;
import com.example.LeadCrawl.services.helpers.ApiCall;
import com.example.LeadCrawl.services.helpers.EmailHandling;
import com.example.LeadCrawl.services.helpers.Helper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("com.example.LeadCrawl.services.implementation.EmailGenerationServiceImpl")
@Slf4j
public class EmailGenerationServiceImpl implements EmailGenerationService {

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Helper")
  private Helper helper;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.EmailHandling")
  private EmailHandling emailHandling;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.ApiCall")
  private ApiCall apiCall;

  @Autowired
  @Qualifier("com.example.LeadCrawl.repository.EmailAccountInformationRepository")
  private EmailAccountInformationRepository emailAccountInformationRepository;

  public List<String> getPatternMap(ProfileDetails profileDetails) {
    String companyDomain = profileDetails.getCompanyDomain();
    companyDomain = helper.getHostName(companyDomain);
    companyDomain = StringUtils.removeEnd(companyDomain, ".");
    String firstName = profileDetails.getFirstName().trim();
    firstName = firstName.replaceAll("\\.","").replaceAll("-","").replaceAll(" ","").replaceAll("'", "");
    String lastName = profileDetails.getLastName().trim();
    lastName = lastName.replaceAll("\\.","").replaceAll("-","").replaceAll(" ","").replaceAll("'", "");
    List<String> emailPatterns = new ArrayList<>();
    if (null!=companyDomain && firstName!=null && firstName.length()!=0){
      if (!companyDomain.equalsIgnoreCase("") ) {
        emailPatterns
                .add((String.format("%s@%s", firstName, companyDomain)).replaceFirst("^\\.", ""));
        emailPatterns.add(
                (String.format("%s%s@%s", firstName, lastName, companyDomain)).replaceFirst("^\\.", ""));
        emailPatterns.add(
                (String.format("%s.%s@%s", firstName, lastName, companyDomain)).replaceFirst("^\\.", "").replaceFirst("\\.@","@"));
        emailPatterns.add((String.format("%s%s@%s", firstName.charAt(0), lastName, companyDomain))
                .replaceFirst("^\\.", ""));
        emailPatterns.add((String.format("%s%s@%s", lastName, firstName.charAt(0), companyDomain))
                .replaceFirst("^\\.", ""));
      }
    }
    return emailPatterns;
  }

  public List<ProfileDetails> getCsvDetails(String csvFilePath) {
    List<ProfileDetails> profileDetailsList = new ArrayList<>();
    try {
      FileReader fileReader = new FileReader(csvFilePath);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] nextRecord;
      int count = 0;
      while ((nextRecord = csvReader.readNext()) != null) {
        if (count == 0) {
          count++;
          continue;
        }
        if (nextRecord.length > 3) {
          ProfileDetails profileDetails = new ProfileDetails();
          profileDetails.setAvatar(nextRecord[5]);
          profileDetails.setId(nextRecord[0]);
          profileDetails.setFullName(nextRecord[1]);
          profileDetails.setCompanyName(nextRecord[7]);
          profileDetails.setFunction(nextRecord[9]);
          profileDetails.setSize(nextRecord[10]);
          profileDetails.setLeadProfile(nextRecord[12]);
          profileDetails.setCompanyDomain(nextRecord[13]);
          profileDetails.setCountry(nextRecord[11]);
          profileDetails.setProfileUrl(nextRecord[2]);
          if (StringUtils.isNotEmpty(nextRecord[3].trim())) {
            profileDetails.setFirstName(nextRecord[3].toLowerCase().replaceAll("\\.", ""));
          } else {
            profileDetails.setFirstName(nextRecord[3]);
          }
          if (nextRecord.length>4) {
            if (StringUtils.isNotEmpty(nextRecord[4].trim())) {
              profileDetails.setLastName(nextRecord[4].toLowerCase().replaceAll("\\.", ""));
            } else {
              profileDetails.setLastName(nextRecord[4]);
            }
          } else {
            profileDetails.setLastName("");
          }
          profileDetails.setDesignation(nextRecord[8]);
          profileDetails.setEmailPattern(getPatternMap(profileDetails));
          profileDetailsList.add(profileDetails);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error( "Error occurred while getting data from CSV. Reason: " +e.toString());
    }
    return profileDetailsList;
  }

  /**
   * generate new csv with Email Ids
   *
   * @param csvFilePath
   */
  @Override
  public String generateEmailForCsv(String csvFilePath) {
    String finalOutputPath = null;
    Set<String> validatedEmails = new HashSet<>();
    Set<String> unValidatedEmails = new HashSet<>();
    List<ProfileDetails> profileDetailsList = getCsvDetails(csvFilePath);
    List<String> emailPatterns = new ArrayList<>();
    for (ProfileDetails profileDetails : profileDetailsList) {
      if(profileDetails.getEmailPattern().size()!=0) {
        for (String i : profileDetails.getEmailPattern()) {
          emailPatterns.add(i);
        }
      }
    }
    log.info("total email patterns: {}",String.valueOf(emailPatterns.size()));
    JSONObject emailPatternsJson = new JSONObject();
    if (emailPatterns.size()!=0){
      emailPatternsJson.put("emailPatterns", emailPatterns);
      JSONObject jsonObject = apiCall.hitCompleteApi(emailPatternsJson);
      validatedEmails.addAll((Collection<? extends String>) jsonObject.get("validatedEmails"));
      unValidatedEmails.addAll((Collection<? extends String>) jsonObject.get("unValidatedEmails"));
      if (validatedEmails.size() == 0 && unValidatedEmails.size() == 0) {
        log.info("No emails are generated after reduction and sending. Creating final CSV.");
        finalOutputPath = createFinalCsv(profileDetailsList,csvFilePath);
      } else {
        unValidatedEmails = setSendingReductionMails(unValidatedEmails);
        updateEmailsInProfileDetails(profileDetailsList, validatedEmails);
        updateEmailsInProfileDetails(profileDetailsList, unValidatedEmails);
        profileDetailsList = removeExtraGeneratedEmails(profileDetailsList);
        finalOutputPath = createFinalCsv(profileDetailsList,csvFilePath);
      }

      log.info("final CSV output successful {} ");
    }
    else {
      log.info("No patterns for email generation for file {}", csvFilePath);
    }


    return finalOutputPath;
  }

  @Override
  public Set<String> setSendingReductionMails(Set<String> unValidatedEmails) {
    Set<String> responseValidMail = new HashSet<>();
    List<String> sendMails = new ArrayList<>();

    List<EmailAccountInformation> setOfMails =
            emailAccountInformationRepository.selectAccount("ACTIVE", "AVAILABLE");
    int numOfEmailsAvailbleToSend = emailHandling.getEmailSendingAvailabilityCount(setOfMails);
    if (numOfEmailsAvailbleToSend < unValidatedEmails.size()){
      log.info("Sufficient Emails not available for email Sending. Please do it again.");
      return null;
    }
    else {
      log.info("Emails to send: {} | total capacity for sending Emails: {}", unValidatedEmails.size(), numOfEmailsAvailbleToSend);
      int count = 0;
      for (String sendMail : unValidatedEmails) {
        sendMails.add(sendMail);
        count++;
        if (count % 20 == 0 && count > 1) {
          Set<String> stringSet = emailHandling.sendAndVerifyEmail(sendMails);
          if (CollectionUtils.isNotEmpty(stringSet)) {
            responseValidMail.addAll(stringSet);
          }
          sendMails.clear();
        }
      }
      if (CollectionUtils.isNotEmpty(sendMails)) {
        Set<String> stringSet = emailHandling.sendAndVerifyEmail(sendMails);
        if (CollectionUtils.isNotEmpty(stringSet)) {
          responseValidMail.addAll(stringSet);
        }
      }
      System.out.println(responseValidMail);
    }
    return responseValidMail;
  }

  private List<ProfileDetails> updateEmailsInProfileDetails( List<ProfileDetails> profileDetailsList,
                                                             Set<String> emails){
    for(ProfileDetails profileDetails: profileDetailsList){
      List<String> emailsInProfile = profileDetails.getEmail();
      if (emailsInProfile==null)
        emailsInProfile = new ArrayList<>();
      for (String i:emails){
        List<String> patterns = profileDetails.getEmailPattern();
        if (patterns.size()!=0) {
          for (String profilePattern : patterns) {
            if (i.equalsIgnoreCase(profilePattern)) {
              emailsInProfile.add(profilePattern);
            }
          }
        }
      }
      profileDetails.setEmail(emailsInProfile);
    }

//    profileDetailsList.stream().forEach(profileDetails ->  {
//      List<String> foundEmail = new ArrayList<>();
//      emails.stream().forEach(email->{
//        List<String> patterns = profileDetails.getEmailPattern();
//        patterns.stream().forEach(pattern->{
//          if (email.equalsIgnoreCase(pattern)){
//            foundEmail.add(email);
//          }
//        });
//      });
//      List<String> emailsInProfile = profileDetails.getEmail();
//      if(foundEmail.size()!=0){
//        foundEmail.stream().forEach(email->{
//          emailsInProfile.add(email);
//        });
//      }
//      profileDetails.setEmail(emailsInProfile);
//    });
    return profileDetailsList;
  }
/////////////////////////////
  private String createFinalCsv(List<ProfileDetails> profileDetailsList,String csvPath) {
    String finalOutputPath = csvPath.replace("output","final-output");
    try {
      FileWriter fileWriter = new FileWriter(finalOutputPath);
      CSVWriter csvWriter = new CSVWriter(fileWriter);
      String[] header = {"NAME", "WEBSITE", "PROFILE_URL", "FIRST_NAME", "LAST_NAME", "MATCH_DESIGNATION", "COUNTRY", "LEAD_LINKEDIN_PROFILE", "COMPANY_DOMAIN", "EMAIL_ID"};
      csvWriter.writeNext(header);
      for (ProfileDetails profileDetails: profileDetailsList){
        String[] nextRecord = {profileDetails.getCompanyName(), profileDetails.getCompanyDomain(),
            profileDetails.getProfileUrl(), profileDetails.getFirstName(),
            profileDetails.getLastName(), profileDetails.getDesignation(), profileDetails.getCountry(), profileDetails.getLeadProfile(), profileDetails.getCompanyDomain(), getListToString(profileDetails.getEmail())};
        csvWriter.writeNext(nextRecord);
        csvWriter.flush();
      }
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
      log.error("Error in final CSV creation. Reason: {}",e.toString());
    }
    return finalOutputPath;
  }

  private String getListToString(List<String> stringList){
    String emailString = "";
    StringBuilder stringBuilder = new StringBuilder();
    if (CollectionUtils.isNotEmpty(stringList)){
      for (int i=0;i<stringList.size();i++){
        if (i<stringList.size()-1) {
          stringBuilder.append(stringList.get(i)).append(",");
        } else {
          stringBuilder.append(stringList.get(i));
        }
      }
      emailString = stringBuilder.toString();
    }
    return emailString;
  }

  /**
   * get priority emails, first two patterns,
   * FirstnameLastname@domain.com, firstname@domain.com
   * @param profileDetails
   * @return emailPatterns
   */
  public List<String> getEmailCheckupMapP1(ProfileDetails profileDetails) {
    String companyDomain = profileDetails.getCompanyDomain();
    companyDomain = helper.getHostName(companyDomain);
    String firstName = profileDetails.getFirstName();
    String lastName = profileDetails.getLastName();
    List<String> emailPatterns = new ArrayList<>();
    if (StringUtils.isNotEmpty(companyDomain)) {
        if (!companyDomain.equalsIgnoreCase("")) {
            emailPatterns.add(String.format("%s@%s", firstName, companyDomain));
            emailPatterns.add(String.format("%s%s@%s", firstName, lastName, companyDomain));
        }
    }
    return emailPatterns;
  }
  public List<String> getEmailCheckupMapP2(ProfileDetails profileDetails) {
    String companyDomain = profileDetails.getCompanyDomain();
    companyDomain = helper.getHostName(companyDomain);
    String firstName = profileDetails.getFirstName();
    String lastName = profileDetails.getLastName();
    List<String> emailPatterns = new ArrayList<>();
      if (StringUtils.isNotEmpty(companyDomain)) {
          if (!companyDomain.equalsIgnoreCase("")) {
              emailPatterns.add(String.format("%s.%s@%s", firstName, lastName, companyDomain));
              emailPatterns.add(String.format("%s%s@%s", firstName.charAt(0), lastName, companyDomain));
          }
      }
    return emailPatterns;
  }
  /**
   * remove extra generated emails
   *
   * @param profileDetailsList
   *
   * @return profileDetailsList
   */
  private List<ProfileDetails> removeExtraGeneratedEmails(List<ProfileDetails> profileDetailsList){
    for (ProfileDetails profileDetails:profileDetailsList) {
      List<String> emailsInProfile = profileDetails.getEmail();
      Set<String> toAddEmails = new HashSet<>();
      List<String> emailsToMapCheckP1 = getEmailCheckupMapP1(profileDetails);
      if (emailsInProfile.size() > 2) {
        emailsInProfile.stream().forEach(email -> {
              if (emailsToMapCheckP1.get(0).equalsIgnoreCase(email) || emailsToMapCheckP1.get(1).equalsIgnoreCase(email)){
                toAddEmails.add(email);
              }
            }
        );
        if (toAddEmails.size()<1){
          List<String> emailsToMapCheckP2 = getEmailCheckupMapP2(profileDetails);
          emailsInProfile.stream().forEach(email -> {
            if (emailsToMapCheckP2.get(0).equalsIgnoreCase(email) || emailsToMapCheckP2.get(1).equalsIgnoreCase(email)){
              toAddEmails.add(email);
            }
          } );
        }
        profileDetails.setEmail(new ArrayList<>(toAddEmails));
      }
    }
    return profileDetailsList;
  }
}

