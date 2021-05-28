package com.example.LeadCrawl.services.helpers;

import com.example.LeadCrawl.entity.EmailAccountInformation;
import com.example.LeadCrawl.enums.Domain;
import com.example.LeadCrawl.model.ProtocolDetails;
import com.example.LeadCrawl.repository.EmailAccountInformationRepository;
import com.example.LeadCrawl.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.example.LeadCrawl.constants.Constants.COMMA;
import static com.example.LeadCrawl.constants.Constants.EMPTY_STRING;
import static com.example.LeadCrawl.constants.Constants.UTF_8;
import static com.example.LeadCrawl.constants.ProtocolConstants.GMAIL_IMAP_HOST;
import static com.example.LeadCrawl.constants.ProtocolConstants.GMAIL_IMAP_PORT;
import static com.example.LeadCrawl.constants.ProtocolConstants.GMAIL_POP3_HOST;
import static com.example.LeadCrawl.constants.ProtocolConstants.GMAIL_POP3_PORT;
import static com.example.LeadCrawl.constants.ProtocolConstants.GMAIL_SMTP_HOST;
import static com.example.LeadCrawl.constants.ProtocolConstants.GMAIL_SMTP_PORT;
import static com.example.LeadCrawl.constants.ProtocolConstants.GMAIL_SPAM_FOLDER;
import static com.example.LeadCrawl.constants.ProtocolConstants.STEERMI_IMAP_HOST;
import static com.example.LeadCrawl.constants.ProtocolConstants.STEERMI_IMAP_PORT;
import static com.example.LeadCrawl.constants.ProtocolConstants.STEERMI_POP3_HOST;
import static com.example.LeadCrawl.constants.ProtocolConstants.STEERMI_POP3_PORT;
import static com.example.LeadCrawl.constants.ProtocolConstants.STEERMI_SMTP_HOST;
import static com.example.LeadCrawl.constants.ProtocolConstants.STEERMI_SMTP_PORT;
import static com.example.LeadCrawl.constants.ProtocolConstants.STEERMI_SPAM_FOLDER;
import static com.example.LeadCrawl.enums.Domain.GMAIL;
import static com.example.LeadCrawl.enums.Domain.MUBILO;
import static com.example.LeadCrawl.enums.Domain.STEERMI;
import static com.example.LeadCrawl.utils.Utils.sleepInSecond;
import static com.example.LeadCrawl.utils.Utils.stringToList;

@Component("com.example.LeadCrawl.services.helpers.EmailHandling")
@Slf4j
public class EmailHandling {
  //  @Value("${email.pattern}")
  private String emailPatternRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9]{2,}";
  private static final String MESSAGE_BLOCKED = ".*message blocked.*";

  //constants for mail
  private static final String SUBJECT = "Schedule Update";
  private static final String MESSAGE_BODY =
          "Hi, \n" + "\n" + "This is regarding our special product launch.\n"
                  + "Please revert in case you are interested.\n" + "\n" + "Thanks\n" + "Raj Kumar";
  private static final String IMAPS = "imaps";
  private static final String POST_OFFICE_PROTOCOL = "pop3";
  private static final String ASTRICK = "*";
  private static final String RECIEVER_PROTOCOL = "pop3s";
  private static final String AT = "@";
  private String imapHost;
  private String imapPort;
  private String pop3Host;
  private String pop3Port;
  private String smtpHost;
  private String smtpPort;
  private String spam;
  private String inbox;
  private static final String INBOX = "INBOX";
  private static final String MULTIPART_TYPE = "multipart/*";
  private Properties smtpProperties;
  private Properties pop3Properties;
  private Properties imapProperties;
  private Pattern emailPattern;
  private static final String MAILER_DAEMON = "mailer-daemon";
  private static final String POSTMASTER = "postmaster";
  private static final String MICROSOFT_EXCHANGE = "microsoftexchange";
  private static final String NOREPLY = "noreply";
  private Pattern rfcPattern = Pattern.compile(".*rfc.*");

  private HashMap<String, Integer> maxAccountMap = new HashMap<>();

  private int TEN = 10;
  private int SIXTY = 60;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Protocol")
  private Protocol protocol;

  @Autowired
  @Qualifier("com.example.LeadCrawl.services.helpers.Notifier")
  private Notifier notifier;


  @Autowired
  @Qualifier("com.example.LeadCrawl.repository.EmailAccountInformationRepository")
  private EmailAccountInformationRepository emailAccountInformationRepository;

  /**
   * Send email
   */
  private boolean sendMail(String toAddress, String username, String password) {
    boolean isMailSend = false;
    try {
      Session session = getSession(smtpProperties, username, password);
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(username));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
      message.setSubject(SUBJECT);
      message.setText(EmailHandling.MESSAGE_BODY);
      Transport.send(message);
      isMailSend = true;
    } catch (Exception e) {
      String errorMessage = "Error occured while sending mails from " + username + " due to " + e;
      notifier.notifySlack(errorMessage);
      log.error("Exception while sending mail: {}", EmailHandling.MESSAGE_BODY, e);
    }
    return isMailSend;
  }

  @PostConstruct
  private void init() {
    emailPattern = Pattern.compile(emailPatternRegex);
    maxAccountMap.put("gmail.com", 80);
    maxAccountMap.put("steermi.com", 180);
    maxAccountMap.put("mubilo.com", 180);
  }

  /**
   * setting confiduration for the protocols
   */

  private void setConfiguration() {
    smtpProperties = new Properties();
    pop3Properties = new Properties();
    imapProperties = new Properties();
    smtpProperties.put("mail.smtp.host", smtpHost);
    smtpProperties.put("mail.smtp.ssl.trust", "*");
    smtpProperties.put("mail.smtp.starttls.enable", "true");
    smtpProperties.put("mail.smtp.socketFactory.port", smtpPort);
    smtpProperties.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.1 TLSv1 SSLv3");
    smtpProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    smtpProperties.put("mail.smtp.ssl.checkserveridentity", false);
    smtpProperties.put("mail.smtp.auth", "true");
    smtpProperties.put("mail.smtp.port", smtpPort);
    smtpProperties.setProperty("mail.smtp.dsn.notify", "FAILURE ORCPT=rfc822;");
    smtpProperties.setProperty("mail.smtp.dsn.ret", "FULL");
    pop3Properties.put("mail.store.protocol", POST_OFFICE_PROTOCOL);
    pop3Properties.put("mail.pop3.host", pop3Host);
    pop3Properties.put("mail.pop3.port", pop3Port);
    pop3Properties.put("mail.pop3.starttls.enable", "true");
    imapProperties.put("mail.store.protocol", IMAPS);
    imapProperties.put("mail.pop3.host", imapHost);
    imapProperties.put("mail.pop3.port", imapPort);
    imapProperties.put("mail.pop3.starttls.enable", "true");
  }

  /**
   * Recieve email and check if any bounce mail present or not
   */
  private Set<String> receiveAndGetStatus(String username, String password,
                                          boolean isAllInboxRead) {
    Set<String> emailList = new HashSet<>();
    try {
      Session pop3Session = Session.getDefaultInstance(pop3Properties);
      Session imapSession = Session.getDefaultInstance(imapProperties);
      Store store = imapSession.getStore(IMAPS);
      store.connect(imapHost, username, password);
      Folder[] folders = store.getDefaultFolder().list(ASTRICK);
      log.info("folders list: {}", folders.toString());
//      getting bounce emails from SPAM folder

      for (Folder folder : folders) {
        if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0 && StringUtils
                .isNotBlank(folder.getFullName()) && folder.getFullName().equalsIgnoreCase(spam)) {
          folder.open(Folder.READ_ONLY);
          // retrieve the messages from the folder in an array and print it
          Message[] messages = folder.getMessages();
          emailList.addAll(getBouncedEmailList(messages, username, isAllInboxRead));
          folder.close(false);
        }
        if (isAllInboxRead) {
          if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0 && StringUtils
                  .isNotBlank(folder.getFullName()) && folder.getFullName().equalsIgnoreCase(inbox)) {
            folder.open(Folder.READ_ONLY);
            // retrieve the messages from the folder in an array and print it
            Message[] messages = folder.getMessages();
            emailList.addAll(getBouncedEmailList(messages, username, isAllInboxRead));
            folder.close(false);
          }
        }
      }

      // create the POP3 store object and connect with the pop server
      //getting bounce emails from INBOX folder
      String domain = username.split("@")[1];
      if(domain.equalsIgnoreCase(Domain.STEERMI.getDomainName())){
        store = imapSession.getStore(IMAPS);
        store.connect(imapHost, username, password);
      }
      else {
        store = pop3Session.getStore(RECIEVER_PROTOCOL);
        store.connect(pop3Host, username, password);
      }
      // create the folder object and open it
      Folder emailFolder = store.getFolder(INBOX);
      emailFolder.open(Folder.READ_ONLY);
      // retrieve the messages from the folder in an array and print it
      Message[] messages = emailFolder.getMessages();
      emailList.addAll(getBouncedEmailList(messages, username, isAllInboxRead));
      // close the store and folder objects
      emailFolder.close(false);
      store.close();
    }
    catch (Exception e) {
      log.error("Exception while receiving mail: {}", e.toString());
      e.printStackTrace();
    }
    return emailList;
  }


  /**
   * return bounce emails
   *
   * @param messages
   * @param username
   *
   * @return
   */
  private Set<String> getBouncedEmailList(Message[] messages, String username,
                                          boolean isAllInboxRead) {
    Set<String> emailList = new HashSet<>();
    Arrays.stream(messages).forEach(message -> {
      StringBuilder stringBuilder = new StringBuilder();
      try {
        String fromAddress = message.getFrom()[0].toString();
        if (Stream.of(POSTMASTER, MAILER_DAEMON, MICROSOFT_EXCHANGE, NOREPLY)
                .anyMatch(fromAddress.toLowerCase()::contains)) {
          writePart(message, stringBuilder);
          String content = stringBuilder.toString().toLowerCase();
          content = content.replace(username, EMPTY_STRING);
          List<String> emailData = stringToList(content);
          boolean isBlocked = isBlocked(emailData);
          if (CollectionUtils.isNotEmpty(emailData)) {
            LinkedHashSet<String> bounceSet = fetchEmails(emailData);
            if (CollectionUtils.isEmpty(bounceSet)) {
              bounceSet = fetchEmailsUsingPattern(emailData);
              if (CollectionUtils.isNotEmpty(bounceSet)) {
                Optional<String> email = bounceSet.stream().findFirst();
                if (email.isPresent()) {
                  bounceSet = new LinkedHashSet<>(Collections.singletonList(email.get()));
                }
              }
            }
            if (isAllInboxRead) {
              if (!isBlocked) {
                emailList.addAll(bounceSet);
              }
            } else {
              emailList.addAll(bounceSet);
            }
          }
        }
        //        if (isAllInboxRead) {
        //          message.setFlag(Flags.Flag.DELETED, true);
        //        }
      } catch (Exception e) {
        log.error("Exception while reading mail", e);
      }
    });
    return emailList;
  }

  /**
   * check if mail is blocked or not
   *
   * @param emailData
   *
   * @return
   */
  private boolean isBlocked(List<String> emailData) {
    boolean isBlocked = false;
    for (String data : emailData) {
      if (data.matches(MESSAGE_BLOCKED)) {
        isBlocked = true;
        break;
      }
    }
    return isBlocked;
  }

  /**
   * fetch email from list of email content
   *
   * @param emailData
   *
   * @return
   */
  private LinkedHashSet<String> fetchEmails(List<String> emailData) {
    LinkedHashSet<String> emailSet = new LinkedHashSet<>();
    for (String email : emailData) {
      Matcher rfcMatcher = rfcPattern.matcher(email);
      if (rfcMatcher.find()) {
        emailPattern = Pattern.compile(emailPatternRegex);
        Matcher matcher = emailPattern.matcher(email);
        if (matcher.find()) {
          emailSet.add(matcher.group());
        }
      }
    }
    return emailSet;
  }

  /**
   * fetching email using email pattern
   *
   * @param emailData
   *
   * @return
   */
  private LinkedHashSet<String> fetchEmailsUsingPattern(List<String> emailData) {
    LinkedHashSet<String> emailSet = new LinkedHashSet<>();
    emailData.forEach(email -> {
      emailPattern = Pattern.compile(emailPatternRegex);
      Matcher matcher = emailPattern.matcher(email);
      if (matcher.find()) {
        emailSet.add(matcher.group());
      }
    });
    return emailSet;
  }

  /**
   * This method checks for content-type
   * based on which, it processes and
   * fetches the content of the message
   *
   * @param part
   * @param stringBuilder
   */
  private static void writePart(Part part, StringBuilder stringBuilder) {
    try {
      if (part.isMimeType(MULTIPART_TYPE)) {
        Multipart mp = (Multipart) part.getContent();
        int count = mp.getCount();
        for (int index = 0; index < count; index++) {
          writePart(mp.getBodyPart(index), stringBuilder);
        }
      } else {
        Object o = part.getContent();
        if (o instanceof InputStream) {
          InputStream is = (InputStream) o;
          String text = IOUtils.toString(is, UTF_8);
          stringBuilder.append(text);
        } else if (o instanceof String) {
          stringBuilder.append((String) o);
        } else {
          stringBuilder.append(o.toString());
        }
      }
    } catch (Exception e) {
      log.debug("Warning while reading mail", e);
    }
  }

  /**
   * Get mail session
   *
   * @return
   */
  private Session getSession(Properties properties, String username, String password) {
    return Session.getInstance(properties, new javax.mail.Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    });
  }

  public Set<String> sendAndVerifyEmail(List<String> emailPatterns) {
    Set<String> response = new HashSet<>();
    if (CollectionUtils.isNotEmpty(emailPatterns)) {
      int mailAddressSize = emailPatterns.size();
      String[] userNameAndPassword = getSenderMails(emailPatterns.size());
      System.out.println(Arrays.toString(userNameAndPassword));
      String username = userNameAndPassword[0];
      String password = userNameAndPassword[1];
      setProperties(username);
      setConfiguration();
      //setting status for all the patterns to true
      Map<String, Boolean> emailMapStatus = new HashMap<>();
      for (String email : emailPatterns) {
        emailMapStatus.put(email, true);
      }
      String emailAddresses = Utils.listToString(emailPatterns, COMMA);
      boolean isMailSend = sendMail(emailAddresses, username, password);
      if (isMailSend) {
        //waiting for email to get bounced
        int perEmailWait = getPerEmailWaitTime(mailAddressSize);
        sleepInSecond(mailAddressSize * perEmailWait);
        //receiving status for the bounced mail
        response = getValidEmails(emailMapStatus, username, password);
      }
    }
    return response;
  }

  private String[] getSenderMails(int size) {
    String[] userNameAndPassword = new String[2];
    List<EmailAccountInformation> setOfMails =
            emailAccountInformationRepository.selectAccount("ACTIVE", "AVAILABLE");
    if (CollectionUtils.isNotEmpty(setOfMails)) {
      userNameAndPassword[0] = setOfMails.get(0).getUserName();
      userNameAndPassword[1] = setOfMails.get(0).getPassword();
      int count = setOfMails.get(0).getCount();
      int updatedCount = count + size;
      String domain = userNameAndPassword[0].split(AT)[1];
      int maxCount = 0;
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date date = new Date();
      if (maxAccountMap.get(domain) != null) {
        maxCount = maxAccountMap.get(domain);
      } else {
        maxCount = 100;
      }
      emailAccountInformationRepository
              .updateCount(userNameAndPassword[0], updatedCount, formatter.format(date));
      if (updatedCount >= maxCount) {
        emailAccountInformationRepository
                .updateJobStatus(userNameAndPassword[0], "UNAVAILABLE", formatter.format(date));
      }
    }
    return userNameAndPassword;
  }

  /**
   * setting properties based on email service provider
   *
   * @param username
   */
  private void setProperties(String username) {
    String domain = username.split(AT)[1];
    System.out.println(domain);
    ProtocolDetails protocolDetails = null;
    ProtocolBuilder protocolBuilder = new ProtocolBuilder();
    //    ProtocolDetails protocolDetails = protocol.getProtocolDetails(domain);
    if (domain.equalsIgnoreCase(GMAIL.getDomainName())) {
      protocolDetails = protocolBuilder.getGmailDetails();
    }
    if (domain.equalsIgnoreCase(STEERMI.getDomainName())) {
      protocolDetails = protocolBuilder.getSteermiDetails();
    }
    if (domain.equalsIgnoreCase(MUBILO.getDomainName())) {
      protocolDetails = protocolBuilder.getMubiloDetails();
    }
    if (null != protocolDetails) {
      this.imapHost = protocolDetails.getImapHost();
      this.imapPort = protocolDetails.getImapPort();
      this.pop3Host = protocolDetails.getPop3Host();
      this.pop3Port = protocolDetails.getPop3Port();
      this.smtpHost = protocolDetails.getSmtpHost();
      this.smtpPort = protocolDetails.getSmtpPort();
      this.spam = protocolDetails.getSpamFolderName();
      this.inbox = protocolDetails.getInboxFolderName();
    }
  }

  /**
   * return bounce response wait time
   *
   * @param mailAddressSize
   *
   * @return
   */
  private int getPerEmailWaitTime(int mailAddressSize) {
    int perEmailWait = TEN;
    if (mailAddressSize < 5) {
      perEmailWait = SIXTY;
    }
    return perEmailWait;
  }

  /**
   * return valid email set
   *
   * @param emailMapStatus
   * @param username
   * @param password
   */
  private Set<String> getValidEmails(Map<String, Boolean> emailMapStatus, String username,
                                     String password) {
    Set<String> response = new HashSet<>();
    Set<String> emailBounced = receiveAndGetStatus(username, password, false);
    //set the flag for bounced mail to false
    if (CollectionUtils.isNotEmpty(emailBounced)) {
      emailBounced.forEach(email -> {
        Boolean flag = emailMapStatus.get(email);
        if (null != flag && flag.equals(true)) {
          emailMapStatus.put(email, false);
        }
      });
    }
    //check if any email do not bounce
    emailMapStatus.forEach((key, value) -> {
      if (value.equals(true)) {
        response.add(key);
      }
    });
    return response;
  }

  /**
   * get the count of emails available for sending
   *
   * @param emailAccountInformationList
   * @return count
   */
  public int getEmailSendingAvailabilityCount(List<EmailAccountInformation> emailAccountInformationList){
    int count=0;
    for (EmailAccountInformation emailAccountInformation: emailAccountInformationList){
      count = count + getCount(emailAccountInformation);
    }
    return count;
  }

  private int getCount(EmailAccountInformation emailAccountInformation){
    String domain = emailAccountInformation.getUserName().split(AT)[1];
    int count = emailAccountInformation.getCount();
    if (count!=0){
      int total_count = maxAccountMap.get(domain);
      count = total_count - count;
      if (count>0)
        return count;
      else
        return 0;
    }
    else{
      count = maxAccountMap.get(domain);
      return count;
    }
  }
}

